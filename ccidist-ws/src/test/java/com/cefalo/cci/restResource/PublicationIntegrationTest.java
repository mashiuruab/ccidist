package com.cefalo.cci.restResource;

import com.sun.jersey.api.client.ClientResponse;

import javax.ws.rs.core.MediaType;

import org.junit.Assert;

import java.net.URI;
import java.util.HashMap;

public class PublicationIntegrationTest extends WebResourceIntegrationTest {
    private final String publicationDetailLink;

    private String accessTokenLink;
    private String uploadLink;
    private String issueSearchLink;

    public PublicationIntegrationTest(final String publicationDetailLink) {
        this.publicationDetailLink = publicationDetailLink;
    }

    @Override
    public void doTest() throws Exception {
        getPublicationDetailTest();
    }

    private void getPublicationDetailTest() {
        getRequestCheck(new HashMap<String, String>(), MediaType.APPLICATION_XHTML_XML, publicationDetailLink, true,
                200, String.format("200 expected from class %s and method %s",
                        PublicationIntegrationTest.class.getName(), "getPublicationDetailTest"), true, true);
        ClientResponse clientResponse = getRequestCheck(new HashMap<String, String>(), MediaType.APPLICATION_XHTML_XML,
                publicationDetailLink, true, 200, "200 expected from publicationDetail Test", true, true);
        String responseString = clientResponse.getEntity(String.class);

        /* This fields are used in the IssueRelatedIntegrationTest */
        accessTokenLink = getXpathStringValue(responseString, ACCESS_TOKEN_EXP);
        issueSearchLink = getXpathStringValue(responseString, ISSUE_SEARCH_LINK_EXP);

        uploadLink = getXpathStringValue(responseString, UPLOAD_LINK_EXP);
        Assert.assertEquals("External upload URL is not valid.", "127.0.0.1", URI.create(uploadLink).getHost());

        getRequestCheck(new HashMap<String, String>(), MediaType.APPLICATION_JSON, publicationDetailLink, true, 406,
                String.format("406 Unsupported mediaType expected from class %s and method %s",
                        PublicationIntegrationTest.class.getName(), "getPublicationDetailTest"), true, true);

        getRequestCheck(new HashMap<String, String>(), MediaType.APPLICATION_XHTML_XML, publicationDetailLink, true,
                200, "200 expected from publicationDetail Test", true, true);
    }

    public String getAccessTokenLink() {
        return accessTokenLink;
    }

    public String getIssueSearchLink() {
        return issueSearchLink;
    }

    public String getUploadLink() {
        return uploadLink;
    }
}
