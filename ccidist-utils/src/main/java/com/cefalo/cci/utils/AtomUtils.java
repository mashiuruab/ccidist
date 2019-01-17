package com.cefalo.cci.utils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.UriBuilder;

import com.sun.syndication.feed.synd.SyndLink;
import com.sun.syndication.feed.synd.SyndLinkImpl;

public abstract class AtomUtils {

    private AtomUtils() {

    }

    public static List<SyndLink> getLinks(
            long start, long limit, long total,
            String issueListUri,
            Map<String, String> queryParams) {
        List<SyndLink> links = new ArrayList<SyndLink>();
        links.add(createAtomLink("self", start, limit, issueListUri, queryParams));

        if (start > 1) {
            // There is a prev link
            links.add(createAtomLink("prev", Math.max(1, start - limit), limit, issueListUri, queryParams));
        }
        if ((start + limit) < (total + 1)) {
            // There is a next link
            links.add(createAtomLink("next", Math.min(start + limit, total), limit, issueListUri, queryParams));
        }

        return links;
    }

    public static List<SyndLink> generateSyndLinks(Map<String, String> relUriMap) {
        List<SyndLink> entryLinks1 = new ArrayList<SyndLink>();
        for (Map.Entry<String, String> entry : relUriMap.entrySet()) {
            SyndLink syndLink = new SyndLinkImpl();
            syndLink.setRel(entry.getKey());
            syndLink.setHref(entry.getValue());
            entryLinks1.add(syndLink);
        }
        return entryLinks1;
    }

    private static SyndLink createAtomLink(
            String relation,
            long start, long limit,
            String issueListUri,
            Map<String, String> queryParams) {
        UriBuilder uriBuilder = UriBuilder.fromUri(URI.create(issueListUri));
        uriBuilder.queryParam("start", start);
        uriBuilder.queryParam("limit", limit);

        if (queryParams != null) {
            for (Entry<String, String> entry : queryParams.entrySet()) {
                try {
                    uriBuilder.queryParam(entry.getKey(), URLEncoder.encode(entry.getValue(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    // UTF-8 is guaranteed by JVM.
                    throw new RuntimeException(e);
                }
            }
        }

        SyndLink self = new SyndLinkImpl();
        self.setRel(relation);
        self.setHref(uriBuilder.build().toString());

        return self;
    }
}
