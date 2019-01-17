package com.cefalo.cci.restResource;

import com.cefalo.cci.utils.XpathHelper;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import org.joda.time.DateTime;
import org.w3c.dom.NodeList;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import javax.xml.xpath.XPathExpressionException;
import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static javax.ws.rs.core.HttpHeaders.IF_MODIFIED_SINCE;
import static junit.framework.Assert.*;

public abstract class WebResourceIntegrationTest extends JerseyTest {
    public static final String BASE_URL = "/webservice";
    public static final String PACKAGE_NAME = "com.cefalo.cci.restResource";
    public static final String SYS_AUTH_SKIP = "com.cefalo.disableAuth";
    public static final String AUTH_TOKEN_NAME = "accesstoken";

    /* This field is for Polaris Organizations detail page link */
    public static final String ORG_LINK_EXP = "html/body/ul[@id='organizations']/li[@id='polaris']/a/@href";

    /* This filed is for Polaris Organization's Addressa Publication's link */
    public static final String PUBLICATION_LINK_EXP = "html/body/ul[@id='publications']/li[@id='addressa']/a/@href";

    /* This fields are used in the IssueRelatedIntegrationTest */
    public static final String ACCESS_TOKEN_EXP = "html/body/div/form[@id='accesstoken']/@action";
    public static final String UPLOAD_LINK_EXP = "html/body/div[@id='uploadcciobjectxml']/dl/dd/a/@href";
    public static final String ISSUE_SEARCH_LINK_EXP = "html/body/div/form[@id='issueSearch']/@action";

    protected WebResource ws;
    protected XpathHelper xpathHelper;

    public WebResourceIntegrationTest() {
        super(new WebAppDescriptor.Builder(PACKAGE_NAME).build());

        // Disable automatic redirection. We don't want a browser.
        client().setFollowRedirects(false);
    }

    protected void conditionalGetsTest(WebResource.Builder builder, boolean isEtagExpected,
            boolean isLastModifiedExpected, NewCookie newCookie) {
        ClientResponse clientResponse = builder.get(ClientResponse.class);

        EntityTag entityTag = clientResponse.getEntityTag();
        if (isEtagExpected) {
            assertNotNull("Etag Header expected: ", entityTag);
        } else {
            assertNull("Etag is not expected here", entityTag);
        }

        Date lastModified = clientResponse.getLastModified();
        if (isLastModifiedExpected) {
            assertNotNull("LastModified Header expected: ", lastModified);
        } else {
            assertNull("LastModified Header is not expected", lastModified);
        }

        if (isEtagExpected) {
            ClientResponse response = etag(builder.cookie(newCookie), entityTag.toString()).get(ClientResponse.class);
            assertEquals(String.format("304 expected If-None-Match header get = %s and send = %s", response
                    .getEntityTag().getValue(), entityTag.getValue()), 304, response.getStatus());

            String randomString = entityTag.getValue().concat(UUID.randomUUID().toString());
            response = etag(builder.cookie(newCookie), String.format("\"%s\"", randomString)).get(ClientResponse.class);
            assertEquals(String.format(
                    "200 expected as random string sent If-None-Match header get = %s and send = %s", response
                            .getEntityTag().getValue(), randomString), 200, response.getStatus());
        }

        if (lastModified != null && isLastModifiedExpected) {
            ClientResponse response = builder.cookie(newCookie).header(IF_MODIFIED_SINCE, lastModified).get(ClientResponse.class);
            assertEquals(
                    String.format("304 expected If-Modified-Since header get = %s and send = %s",
                            response.getLastModified(), lastModified), 304, response.getStatus());

            DateTime prevDate = new DateTime(lastModified.getTime());
            prevDate = prevDate.minusYears(1);
            response = builder.cookie(newCookie).header(HttpHeaders.IF_MODIFIED_SINCE, prevDate.toDate()).get(ClientResponse.class);
            assertEquals(
                    String.format("200 expected as If-Modified-Since header smaller get = %s and send = %s",
                            response.getLastModified(), prevDate.toDate()), 200, response.getStatus());

            DateTime futureDate = new DateTime(lastModified.getTime());
            futureDate = futureDate.plusYears(1);
            response = builder.cookie(newCookie).header(HttpHeaders.IF_MODIFIED_SINCE, futureDate.toDate()).get(ClientResponse.class);
            assertEquals(String.format("304 expected as If-Modified-Since header future date get = %s and send = %s",
                    response.getLastModified(), futureDate.toDate()), 304, response.getStatus());
        }

        if (entityTag != null && lastModified != null && isEtagExpected && isLastModifiedExpected) {
            ClientResponse etagLastModified = builder.cookie(newCookie)
                    .header(HttpHeaders.IF_NONE_MATCH, "\"" + entityTag.getValue() + "\"")
                    .header(HttpHeaders.IF_MODIFIED_SINCE, lastModified).get(ClientResponse.class);
            assertEquals("304 expected for both cases: ", 304, etagLastModified.getStatus());

            DateTime futureDate = new DateTime(lastModified.getTime());
            futureDate = futureDate.plusYears(1);
            etagLastModified = builder.cookie(newCookie).header(HttpHeaders.IF_NONE_MATCH, "\"" + entityTag.getValue() + "\"")
                    .header(HttpHeaders.IF_MODIFIED_SINCE, futureDate.toDate()).get(ClientResponse.class);
            assertEquals(String.format("304 expected for both cases: etag matched and If-Modified-Since in future "
                    + "(get, send) etag (%s, %s)and (get, send) lastModified (%s, %s)", etagLastModified.getEntityTag()
                    .getValue(), entityTag.getValue(), etagLastModified.getLastModified(), futureDate.toDate()), 304,
                    etagLastModified.getStatus());

            String randomString = entityTag.getValue().concat(UUID.randomUUID().toString());
            etagLastModified = builder.cookie(newCookie).header(HttpHeaders.IF_NONE_MATCH, "\"" + randomString + "\"")
                    .header(HttpHeaders.IF_MODIFIED_SINCE, lastModified).get(ClientResponse.class);
            assertEquals("200 expected for both cases: etag random string send", 200, etagLastModified.getStatus());

            DateTime prevDate = new DateTime(lastModified.getTime()).minusYears(1);
            etagLastModified = builder.cookie(newCookie).header(HttpHeaders.IF_NONE_MATCH, "\"" + entityTag.getValue() + "\"")
                    .header(HttpHeaders.IF_MODIFIED_SINCE, prevDate.toDate()).get(ClientResponse.class);
            assertEquals("200 expected for both cases: etag matched and If-Modified-Since in past", 200,
                    etagLastModified.getStatus());
        }

    }

    protected ClientResponse getRequestCheck(Map<String, String> queryParamMap, String mediaType, String uri,
            boolean isAbsoluteUri, int expectedStatus, String message, boolean isEtagExpected,
            boolean isLastModifiedExpected) {
        if (isAbsoluteUri) {
            ws = resource().uri(URI.create(uri));
        } else {
            ws = resource().path(BASE_URL).path(uri);
        }
        for (Map.Entry<String, String> entry : queryParamMap.entrySet()) {
            ws = ws.queryParam(entry.getKey(), entry.getValue());
        }
        ClientResponse clientResponse = ws.accept(mediaType).get(ClientResponse.class);
        assertEquals(message, expectedStatus, clientResponse.getStatus());
        if (clientResponse.getStatus() == 200) {
            conditionalGetsTest(ws.accept(mediaType), isEtagExpected, isLastModifiedExpected, null);
        }
        return clientResponse;
    }

    protected String getXpathStringValue(String srcString, String pattern) {
        xpathHelper = new XpathHelper(srcString);
        String value = null;
        try {
            value = xpathHelper.parseSingleNodeValue(pattern);
        } catch (XPathExpressionException e) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
        }
        return value;
    }

    protected NodeList getNodeListCountCheck(String response, String pattern, int expectedCount, String message)
            throws XPathExpressionException {
        xpathHelper = new XpathHelper(response);
        NodeList nodeList = xpathHelper.getNodeListFromHtml(pattern);
        assertEquals(message, expectedCount, nodeList.getLength());
        return nodeList;
    }

    protected WebResource.Builder etag(WebResource.Builder builder, String etag) {
        return builder.header(HttpHeaders.IF_NONE_MATCH, etag);
    }

    public abstract void doTest() throws Exception;
}
