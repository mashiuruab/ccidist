package com.cefalo.cci.restResource;

import com.sun.jersey.api.client.ClientResponse;
import org.w3c.dom.NodeList;

import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPathExpressionException;
import java.net.URI;
import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class OrganizationIntegrationTest extends WebResourceIntegrationTest {
    private String organizationDetailLink;
    private String publicationDetailLink;

    @Override
    public void doTest() throws Exception {
        getOrganizationListTest();
        getOrganizationDetailTest();
    }

    private void getOrganizationListTest() throws XPathExpressionException {
        ws = resource().path(BASE_URL).path("/");
        ClientResponse clientResponse = ws.accept(MediaType.APPLICATION_XHTML_XML).get(ClientResponse.class);
        String responseHtml = clientResponse.getEntity(String.class);
        assertEquals(200, clientResponse.getStatus());
        assertNotNull(responseHtml);

        NodeList nodeList = getNodeListCountCheck(responseHtml, "html/body/ul/li", 3,
                "organization list page: number of url's expected");

        List<String> actualList = new ArrayList<String>();
        actualList.add("AxelSpringer");
        actualList.add("NHST");
        actualList.add("Polaris");

        List<String> expectedList = new ArrayList<String>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            expectedList.add(nodeList.item(i).getTextContent());
        }

        Collections.sort(expectedList);
        assertEquals(actualList, expectedList);

        organizationDetailLink = getXpathStringValue(responseHtml, ORG_LINK_EXP);

        getRequestCheck(new HashMap<String, String>(), MediaType.APPLICATION_XHTML_XML, "@#&*//*", false, 404,
                String.format("404 expected Class %s and Method %s", OrganizationIntegrationTest.class.getName(),
                        "getOrganizationListTest()"), true, false);

        getRequestCheck(new HashMap<String, String>(), MediaType.APPLICATION_XML, "/", false, 406, String.format(
                "406 error expected Class %s and Method %s", OrganizationIntegrationTest.class.getName(),
                "getOrganizationListTest()"), true, false);

        getRequestCheck(new HashMap<String, String>(), MediaType.APPLICATION_XHTML_XML, "/", false, 200,
                "200 expected from OrganizationList test", true, false);
    }

    private void getOrganizationDetailTest() throws XPathExpressionException {
        getRequestCheck(new HashMap<String, String>(), MediaType.APPLICATION_XHTML_XML, "/polaris$#@", false, 404,
                String.format("404 expected Class %s and Method %s", OrganizationIntegrationTest.class.getName(),
                        "getOrganizationDetailTest()"), true, false);

        ws = resource().uri(URI.create(getOrganizationDetailLink()));
        ClientResponse clientResponse = ws.accept(MediaType.APPLICATION_XHTML_XML).get(ClientResponse.class);
        String responseString = ws.accept(MediaType.APPLICATION_XHTML_XML).get(String.class);
        assertEquals("status found: ", 200, clientResponse.getStatus());
        assertNotNull(responseString);

        NodeList nodeList = getNodeListCountCheck(responseString, "html/body/ul/li", 2,
                "Organization Detail Page: number of publication expected");

        List<String> actualList = Arrays.asList("Addressa", "Harstadtidende");
        List<String> expectedList = new ArrayList<String>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            expectedList.add(nodeList.item(i).getTextContent());
        }
        Collections.sort(expectedList);
        assertEquals(actualList, expectedList);

        publicationDetailLink = getXpathStringValue(responseString, PUBLICATION_LINK_EXP);

        getRequestCheck(new HashMap<String, String>(), MediaType.APPLICATION_XML, getOrganizationDetailLink(), true,
                406, String.format("406 expected Class %s and Method %s", OrganizationIntegrationTest.class.getName(),
                        "getOrganizationDetailTest()"), true, false);

        getRequestCheck(new HashMap<String, String>(), MediaType.APPLICATION_XHTML_XML, getOrganizationDetailLink(),
                true, 200, "200 expected from OrganizationDetail Test: ", true, false);
    }

    public String getOrganizationDetailLink() {
        return organizationDetailLink;
    }

    public String getPublicationDetailLink() {
        return publicationDetailLink;
    }
}
