# This is the VCL file for CCI Distribution Service. Please review the comments
# below to get a better idea on how to use this.

### REQUIREMENT: Varnish 3.x. Has been tested with v3.0.3 & v3.0.4.
###
### REQUIREMENT: This VCL file does not require varnish to be compiled from
### source. You are free to use your distributions varnish package. But the
### Digest & Cookie VMODs must be installed & configured properly to use this
### VCL.
###
### Instructions for installing the Digest VMOD can be found at:
### https://www.varnish-cache.org/vmod/digest
###
### Instructions for installing the Cookie VMOD can be found at:
### https://www.varnish-cache.org/vmod/cookie
###
### Instructions for installing the URL Code VMOD can be found at:
### https://www.varnish-cache.org/vmod/url-code
###
### REQUIREMENT: Varnish default TTL should be set to a high value. As a
### default varnish uses 120s. However, the backend response cache TTL is the
### minimum of this & the VCL configured value. So, please ensure that varnishd
### process is started with "-t 3600".
###
### For installing varnish from source:
### https://www.varnish-cache.org/docs/3.0/installation/install.html#compiling-varnish-from-source
###
### IMPORTANT: All the configurable values in this VCL file are marked by a
### comment with prefix "### CHANGE:: ". Use your favorite "grep" tool :-)
#

import std;
import digest;
import cookie;
import urlcode;

# Default backend definition.  Set this to point to your CCI Distribution
# server.
#
### CHANGE:: Change the host/port of your /webservice backend.
backend default {
    .host = "localhost";
    .port = "8080";
}

### CHANGE:: ACL of IPs/hostnames that are allowd to BAN/PURGE
acl purge_acl {
     "10"/8;
     "172.16"/12;
     "192.168"/16;
}

sub setup_constants {
    ### CHANGE:: req.http.SKIP_AUTHENTICATION to enable/disable the authentication.
    set req.http.SKIP_AUTHENTICATION = "false";

    ### CHANGE:: req.http.SECRET_KEY to configure the secret key. This must be
    ### the exact same value as the "authenticationSecret" config parameter in
    ### CCI Distribution "Application.properties" file.
    ###
    set req.http.SECRET_KEY = "secret";

    ### CHANGE:: The default cache TTL. It is usually OK to set this to a high
    ### value. However, a conservative 10 minutes is used as default.
    ###
    set req.http.EPUB_TTL = "10m";
    set req.http.PUBLIC_ISSUE_TTL = "10m";
    set req.http.ISSUE_GENERAL_TTL = "10m";
    set req.http.STD_TTL = "10m";

    ### CHANGE:: AccessToken expire/invalid LOGIN_URL. The actual requested URL
    ### will be appended with this.
    ###
    set req.http.LOGIN_URL = "http://example.com/ccidist-portal/accesstoken?referer=";
}

sub unset_custom_headers {
    # The temporary variables
    unset req.http.accesstoken;
    unset req.http.signature;
    unset req.http.tokenTimestamp;
    unset req.http.productId;
    unset req.http.currentTime;

    unset req.http.unauthorized;

    # These are used as temp vars in vcl_deliver
    unset req.http.resourcePublication;
    unset req.http.resourceIssueID;
    unset req.http.tokenPublication;
    unset req.http.tokenIssueID;
    unset req.http.cookiePath;

    # The CONSTANTS
    unset req.http.SKIP_AUTHENTICATION;
    unset req.http.EPUB_TTL;
    unset req.http.PUBLIC_ISSUE_TTL;
    unset req.http.ISSUE_GENERAL_TTL;
    unset req.http.STD_TTL;
    unset req.http.SECRET_KEY;
    unset req.http.LOGIN_URL;
}

sub initialize_vars {
    call unset_custom_headers;
    call setup_constants;
}

sub webapp_filter {
    if (req.url ~ "/ccidist-portal(?:$|/.*)") {
        std.log("CCIDIST_LOG:: ccidist-portal pass" + req.url);
        return (pass);
    }

    if (req.url ~ "/ccidist-reader/.*") {
        std.log("CCIDIST_LOG:: ccidist-reader pass" + req.url);
        return (pass);
    }

    if (req.url !~ "/ccidist-ws(?:$|/.*)") {
        std.log("CCIDIST_LOG:: Error 404 not on whitelist: " + req.url);
        error 404 "Not Found";
    }
}

sub vcl_recv {
    call webapp_filter;

    # This may happen when the request is restarted from vcl_deliver
    if (req.http.unauthorized == "true") {
        error 751 "Unauthorized";
    }

    if (req.request != "GET") {
       call unset_custom_headers;
    }

    call normalize_ccidist_url;

    if (req.request == "BAN") {
        if (client.ip ~ purge_acl) {
            std.log("CCIDIST_LOG:: BAN request from: " + client.ip + " for: " + req.url);

            if (req.url ~ ".*(\/issues\/$)") {
                # We'll basically add a BAN for the issue search page.
                ban("req.http.host == " + req.http.host + " && req.url ~ " + req.url + ".*(epubName=).*");
                std.log("CCIDIST_LOG:: BAN added for issue search URL: " + req.url);

                error 200 "Ban added";
            } else {
                # We basically BAN everything from that URL down.
                ban("req.http.host == " + req.http.host + " && req.url ~ " + req.url + ".*");
                std.log("CCIDIST_LOG:: BAN added for issue: " + req.url);

                error 200 "Ban added";
            }
        } else {
            error 403 "BAN not allowed from the client: " + client.ip;
        }
    } else if (req.request == "PURGE") {
        if (client.ip ~ purge_acl) {
            std.log("CCIDIST_LOG:: PURGE request from: " + client.ip + " for: " + req.url);
            return (lookup);
        } else {
            error 403 "PURGE not allowed from the client: " + client.ip;
        }
    }else if (req.request == "GET") {
        call initialize_vars;

        if (req.http.SKIP_AUTHENTICATION == "false") {
            call process_access_token;
            if(req.http.accesstoken && req.http.accesstoken != "") {
                call do_authentication;
            } else {
                # There is accesstoken. This user is not authenticated/authorized.
                set req.http.unauthorized = "true";
            }

            # Unset the secret key
            unset req.http.SECRET_KEY;

            # Cookies are removed to make things cache-able.
            unset req.http.Cookie;

            # Setting cache time for different URL patterns
            if (req.url ~ "(.+/.*(.epub$))") {
                set req.ttl = std.duration(req.http.EPUB_TTL, 600s);
            } else if (req.url ~ "(.+/issues/.+$)") {
                if (req.url ~ "(.+/issues/.+/public$)") {
                    set req.ttl = std.duration(req.http.PUBLIC_ISSUE_TTL, 600s);
                } else {
                    set  req.ttl = std.duration(req.http.ISSUE_GENERAL_TTL, 600s);
                }
            } else {
                set req.ttl = std.duration(req.http.STD_TTL, 600s);
            }
        } else {
            call unset_custom_headers;
        }
    } else if (req.request == "POST") {
        return (pass);
    }
}

sub normalize_ccidist_url {
    # Clean out requests sent via curls -X mode and LWP
    if (req.url ~ "^http://") {
        set req.url = regsub(req.url, "http://[^/]*", "");
    }

    # Remove double // in urls,
    set req.url = regsuball( req.url, "//", "/"      );

    # Normalizing the host header
    if(req.http.host ~ "^.*:80$") {
        set req.http.host = regsub(req.http.host, ":80$", "");
    }
}

sub process_access_token {
    # Extract access token from query param. Also, cleanup the URL.
    if(req.url ~ "(&|\?)accesstoken=[^/]*$") {
        set req.http.accesstoken = regsub(req.url, "^.*accesstoken=([\w|\||:|\*|%7c|%7C|%3a|%3A]*).*$", "\1");
        std.log("CCIDIST_LOG:: Accesstoken from URL: " + req.http.accesstoken);

        # Remove the accesstoken query param from the URL
        set req.url = regsub(req.url, "(accesstoken=[\w|\||:|\*|%7c|%7C|%3a|%3A]*&?)", "");

        # Remove any trailing ? or &
        set req.url = regsub(req.url, "[\?|&]$", "");

        std.log("CCIDIST_LOG:: URL after removing accesstoken query param: " + req.url);
    } else {
        std.log("CCIDIST_LOG:: No accesstoken in URL. Will check Cookies: " + req.http.Cookie);

        # Lets check the cookie for an access token
        cookie.parse(req.http.Cookie);
        set req.http.accesstoken = cookie.get("accesstoken");
        cookie.clean();

        std.log("CCIDIST_LOG:: Accesstoken in Cookie: " + req.http.accesstoken);
    }

    if (req.http.accesstoken && req.http.accesstoken != "") {
        set req.http.accesstoken = urlcode.decode(req.http.accesstoken);
        std.log("CCIDIST_LOG:: Decoded access token: " + req.http.accesstoken);

        call extract_token_parts;
    } else {
        # Just to make req.http.accesstoken = null [Java Speak]
        unset req.http.accesstoken;

        std.log("CCIDIST_LOG:: No access token found.");
    }
}

sub extract_token_parts {
    set req.http.signature = regsub(req.http.accesstoken, "(\|.*)", "");
    set req.http.tokenTimestamp = regsub(req.http.accesstoken, ".*\|(.*)\|.*" , "\1");
    set req.http.productId = regsub(req.http.accesstoken, ".*\|.*\|(.*)" , "\1");

    std.log("CCIDIST_LOG:: Token Product ID   = " + req.http.productId);
    std.log("CCIDIST_LOG:: Token Timestamp    = " + req.http.tokenTimestamp);
    std.log("CCIDIST_LOG:: Token Signature    = " + req.http.signature);
}

sub do_authentication {
    set req.http.tmp_generatedSignature = digest.hash_md5(req.http.SECRET_KEY + "|" + req.http.tokenTimestamp + "|" + req.http.productId);
    std.log("CCIDIST_LOG:: Verified signature = " + req.http.tmp_generatedSignature);

    if (req.http.signature != req.http.tmp_generatedSignature) {
        std.log("CCIDIST_LOG:: The access token is invalid. The crypt-signature did not match.");
        unset req.http.tmp_generatedSignature;
        unset req.http.accesstoken;

        set req.http.unauthorized = "true";
    } else {
        # Check if token has expired
        set req.http.currentTime = now + 0s;
        set req.http.currentTime = regsub(req.http.currentTime, "\..*", "");
        std.log("CCIDIST_LOG:: NOW = " + req.http.currentTime);

        if (std.integer(req.http.currentTime, 0) > std.integer(req.http.tokenTimestamp, 0)) {
            # Although the token was generate by us, it has expired.
            std.log("CCIDIST_LOG:: The token has expired.");

            set req.http.unauthorized = "true";
        } else {
            # All is well.
            set req.http.unauthorized = "false";
        }

        unset req.http.tmp_generatedSignature;
    }
}

sub vcl_hit {
    if (req.request == "PURGE") {
        purge;
        error 200 "Purged.";
    }
}

sub vcl_miss {
    if (req.request == "PURGE") {
        purge;
        error 200 "Purged.";
    }
}

sub vcl_deliver {
    # If authentication is enabled && response is OK && there was a product id on the response...
    if (req.http.SKIP_AUTHENTICATION == "false"
            && resp.status == 200
            && resp.http.X-SP-Pids) {
        # If the user is not authorized, we restart the request.
        if (req.http.unauthorized == "true") {
            return (restart);
        }

        # The resource has a Product ID. We need to match this against the token PID.
        # The token can be for a specific issue or a whole publication. The format for
        # the whole publication token is "publication_name:*".

        set req.http.tokenPublication = regsub(req.http.productId, "(.*):(?:\*|\d+)", "\1");
        set req.http.tokenIssueID = regsub(req.http.productId, ".*:(\*|\d+)", "\1");

        set req.http.resourcePublication = regsub(resp.http.X-SP-Pids, "(.*):\d+", "\1");
        set req.http.resourceIssueID = regsub(resp.http.X-SP-Pids, ".*:(\d+)", "\1");

        if (req.http.resourcePublication != req.http.tokenPublication
                || (req.http.tokenIssueID != req.http.resourceIssueID && req.http.tokenIssueID != "*")) {
            set req.http.unauthorized = "true";
            return (restart);
        }

        # We need to set the Set-Cookie header with the right path here. The right
        # path depends on the token type. It can be for a single issue or the full
        # publication.
        if (std.integer(req.http.tokenIssueID, 0) != 0) {
            # Set the Cookie for a specific issue.
            set req.http.cookiePath = regsub(req.url, "^(.*\/issues\/[\d]+\/).*$", "\1");
        } else if (req.http.tokenIssueID == "*") {
            # Set the Cookie for a whole publication.
            set req.http.cookiePath = regsub(req.url, "^(.*\/issues\/)[\d]+\/.*$", "\1");
        } else {
            std.log("CCIDIST_LOG:: I really wasn't prepared for this :-(");

            set req.http.unauthorized = "true";
            return (restart);
        }

        set resp.http.Set-Cookie = "accesstoken=" + req.http.accesstoken
                + ";Path=" + req.http.cookiePath
                + ";Version=1;Max-Age=3600";

        call unset_custom_headers;
    }
}

sub vcl_error {
    # If the user is not properly authenticated, we redirect to the LOGIN_URL.
    if (obj.status == 751) {
        set obj.http.Location = req.http.LOGIN_URL + urlcode.encode("http://" + req.http.host + req.url);
        set obj.status = 302;
        return(deliver);
    }

    set obj.http.Content-Type = "text/html; charset=utf-8";
    set obj.http.Retry-After = "5";

    # synthetic STARTS
    synthetic {"
<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
    <head>
        <title>"} + obj.status + " " + obj.response + {"</title>
    </head>
    <body>
        <h1>Status "} + obj.status + " " + obj.response + {"</h1>
        <p>"} + obj.response + {"</p>
        <hr>
        <p>CCI Distribution Service</p>
    </body>
</html>

    "};
    # synthetic ENDS

    return (deliver);
}
