package com.cefalo.cci.restResource;

import com.cefalo.cci.model.token.AccessToken;
import com.cefalo.cci.utils.StringUtils;
import com.cefalo.cci.utils.XpathHelper;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.hash.Hashing;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.representation.Form;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.NodeList;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.xml.xpath.XPathExpressionException;

import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static junit.framework.Assert.*;
import static org.apache.http.HttpStatus.SC_OK;

public class IssueRelatedIntegrationTest extends WebResourceIntegrationTest {
    private final String redirectURLWithQueryKey = "http://portal/refresh_access_token?referer=";
    private final String SECRET_KEY = "secret";
    private final String uploadLink;
    private final String accessTokenLink;
    private final String issueSearchLink;

    public IssueRelatedIntegrationTest(String uploadLink, String accessTokenLink, String issueSearchLink) {
        this.uploadLink = uploadLink;
        this.accessTokenLink = accessTokenLink;
        this.issueSearchLink = issueSearchLink;
    }

    @Override
    public void doTest() throws Exception {
        uploadRxmlFiles();
    }

    /* Used from the rxmlFileUpload method */
    private void downloadEpubTest(Map<String, String> issueDloadLinkMap) throws IOException {
        getRequestCheck(new HashMap<String, String>(), MediaType.TEXT_PLAIN,
                "/polaris/addressa/issues/100/content.epub",
                false, 404, "404 expected from downloadEpubTest:", false, false);

        getRequestCheck(new HashMap<String, String>(), MediaType.TEXT_PLAIN, "/polaris/  /issues/101/content.epub",
                false, 400,
                "400 expected from downloadEpubTest: ", false, false);

        getRequestCheck(new HashMap<String, String>(), MediaType.TEXT_PLAIN,
                "/polaris/testing/issues/102/content.epub", false,
                404, "404 expected from downloadEpubTest", false, false);

        getRequestCheck(new HashMap<String, String>(), MediaType.APPLICATION_OCTET_STREAM,
                "/polaris/testing/issues/103/content.epub", false, 406, "406 expected from downloadEpubTest", false,
                false);

        for (Map.Entry<String, String> issueDloadLink : issueDloadLinkMap.entrySet()) {
            String dloadLink = issueDloadLink.getValue();
            String token = getAccessTokenTest(dloadLink);

            ClientResponse clientResponse = accesstokenCookieTest(dloadLink, token, 200, MediaType.TEXT_PLAIN,
                    "epub download request 200 expexted: ");
            try (InputStream input = clientResponse.getEntity(InputStream.class)) {
                int data = input.read();
                while (data != -1) {
                    data = input.read();
                }
            }
        }
    }

    /* Used from the rxmlFileUpload method */
    private void getEpubContentTest(Map<String, String[]> issueEpubContentLinkMap) {
        getRequestCheck(new HashMap<String, String>(), MediaType.APPLICATION_XML,
                "/polaris/addressa/issues/100/META-INF/container.xml", false, 404,
                "404 expected from getEpubContentTest()", true, true);
        ClientResponse clientResponse;

        getRequestCheck(new HashMap<String, String>(), MediaType.APPLICATION_XML,
                "/ /addressa/issues/ /META-INF/container.xml", false, 400, "400 expected from getEpubContentTest()",
                true, true);

        getRequestCheck(new HashMap<String, String>(), MediaType.APPLICATION_XML,
                "/polaris/addressa/issue/100/META-INF/con.xml", false, 404, "404 expected from getEpubContentTest()",
                true, true);

        getRequestCheck(new HashMap<String, String>(), MediaType.APPLICATION_XML,
                "/polaris/addressa/issues/100/META-INF/", false, 404, "404 expected from getEpubContentTest()", true,
                true);

        for (Map.Entry<String, String[]> issueEpubContentLink : issueEpubContentLinkMap.entrySet()) {
            String[] epubContentLink = issueEpubContentLink.getValue();
            String containerXmlLinkToken = getAccessTokenTest(epubContentLink[0]);
            clientResponse = accesstokenCookieTest(epubContentLink[0], containerXmlLinkToken, 200,
                    MediaType.APPLICATION_XML,
                    "Content found code xml file: ");
            assertEquals("Content found code xml file: ", 200, clientResponse.getStatus());
            assertNotNull(clientResponse);
            assertTrue(String.format("%s mimeType expected: ", MediaType.APPLICATION_XML), clientResponse.getType()
                    .equals(MediaType.APPLICATION_XML_TYPE));

            NewCookie cookie = new NewCookie(AUTH_TOKEN_NAME, containerXmlLinkToken);
            ws = resource().uri(URI.create(epubContentLink[0]));
            conditionalGetsTest(ws.cookie(cookie).accept(MediaType.APPLICATION_XML), true, true, cookie);

            cookie = new NewCookie(AUTH_TOKEN_NAME, getAccessTokenTest(epubContentLink[1]));
            ws = resource().uri(URI.create(epubContentLink[1]));
            clientResponse = ws.cookie(cookie).get(ClientResponse.class);
            assertEquals("Content found code css file: " + epubContentLink[1], 200, clientResponse.getStatus());
            assertNotNull(clientResponse);
            MediaType expectedMediaType = new MediaType("text", "css");
            assertTrue("text/css mimeType expected: ", clientResponse.getType().equals(expectedMediaType));

            ws = resource().uri(URI.create(epubContentLink[1]));
            conditionalGetsTest(ws.cookie(cookie).accept("text/css"), true, true, cookie);

            cookie = new NewCookie(AUTH_TOKEN_NAME, getAccessTokenTest(epubContentLink[2]));
            ws = resource().uri(URI.create(epubContentLink[2]));
            clientResponse = ws.cookie(cookie).get(ClientResponse.class);
            assertEquals("Content found code xhtml file: " + epubContentLink[2], 200, clientResponse.getStatus());
            assertNotNull(clientResponse);
            assertTrue(String.format("%s mimeType expected but found %s: ", MediaType.APPLICATION_XHTML_XML,
                    clientResponse.getType()), clientResponse.getType().equals(MediaType.APPLICATION_XHTML_XML_TYPE));

            ws = resource().uri(URI.create(epubContentLink[2]));
            conditionalGetsTest(ws.cookie(cookie).accept(MediaType.APPLICATION_XHTML_XML), true, true, cookie);

            cookie = new NewCookie(AUTH_TOKEN_NAME, getAccessTokenTest(epubContentLink[3]));
            ws = resource().uri(URI.create(epubContentLink[3]));
            clientResponse = ws.cookie(cookie).get(ClientResponse.class);
            assertEquals("Content found code json file: " + epubContentLink[3], 200, clientResponse.getStatus());
            assertNotNull(clientResponse);
            assertTrue(
                    String.format("%s mimeType expected but found %s: ", MediaType.APPLICATION_JSON,
                            clientResponse.getType()), clientResponse.getType().equals(MediaType.APPLICATION_JSON_TYPE));

            ws = resource().uri(URI.create(epubContentLink[3]));
            conditionalGetsTest(ws.cookie(cookie).accept(MediaType.APPLICATION_JSON), true, true, cookie);
        }
    }

    /* Used from the rxmlFileUpload method */
    private void getIssueListTest() throws XPathExpressionException {
        /*
         * NOTE: Here fromDate used to fetch all issues starting from this date. So this Integration test only tests on
         * our supplied epub files in project directory.
         */

        String deviceType = "ipad2";
        DateTime toDate = new DateTime().withTimeAtStartOfDay().plusDays(1);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        String toDateStr = fmt.print(toDate);

        ws = resource().uri(URI.create(issueSearchLink)).queryParam("epubName", "ipad2")
                .queryParam("toDate", toDateStr).queryParam("draft", "true");
        ClientResponse clientResponse = ws.accept(MediaType.APPLICATION_ATOM_XML).get(ClientResponse.class);
        assertEquals("content found code: ", 200, clientResponse.getStatus());

        clientResponse = ws.accept(MediaType.APPLICATION_XML).get(ClientResponse.class);
        assertEquals("Not acceptable error code: ", 406, clientResponse.getStatus());

        String responseString = ws.accept(MediaType.APPLICATION_ATOM_XML).get(String.class);
        assertNotNull(responseString);
        String feedEntryExp = "feed/entry";

        getNodeListCountCheck(responseString, feedEntryExp, 1, "Default number of entry: ");

        // TODO: 404 error should be checked
        /*
         * ws = resource().path(BASE_URL).path("polaris").path("addressa").path("issue"); clientResponse =
         * ws.accept(MediaType.APPLICATION_ATOM_XML).get(ClientResponse.class); assertEquals("Html code not found: ",
         * 404, clientResponse.getStatus());
         */

        getRequestCheck(new HashMap<String, String>(), MediaType.APPLICATION_ATOM_XML, issueSearchLink, true, 400,
                "400 expected method getIssueListTest()", false, false);

        Map<String, String> queryMap = new HashMap<String, String>();
        queryMap.put("start", "1");
        queryMap.put("limit", "10");
        queryMap.put("epubName", "ipad3");
        queryMap.put("toDate", toDateStr);
        queryMap.put("draft", "true");

        clientResponse = getRequestCheck(queryMap, MediaType.APPLICATION_ATOM_XML, issueSearchLink, true, 200,
                "Issue Search list test #1", false, false);
        responseString = clientResponse.getEntity(String.class);
        assertNotNull(responseString);
        getNodeListCountCheck(responseString, feedEntryExp, 2, "number of entry start 1 for ipad3: ");

        queryMap = new HashMap<String, String>();
        queryMap.put("start", "1");
        queryMap.put("limit", "10");
        queryMap.put("epubName", "ipad-mini");
        queryMap.put("toDate", toDateStr);
        queryMap.put("draft", "true");
        clientResponse = getRequestCheck(queryMap, MediaType.APPLICATION_ATOM_XML, issueSearchLink, true, 200,
                "Issuu Search list test #2", false, false);
        responseString = clientResponse.getEntity(String.class);
        assertNotNull(responseString);
        getNodeListCountCheck(responseString, feedEntryExp, 2, "number of entry start 1 for ipad-mini: ");

        queryMap = new HashMap<String, String>();
        queryMap.put("start", "1");
        queryMap.put("limit", "10");
        queryMap.put("epubName", deviceType);
        queryMap.put("toDate", toDateStr);
        queryMap.put("draft", "true");
        clientResponse = getRequestCheck(queryMap, MediaType.APPLICATION_ATOM_XML, issueSearchLink, true, 200,
                "Issuu Search list test #3", false, false);
        responseString = clientResponse.getEntity(String.class);
        assertNotNull(responseString);
        getNodeListCountCheck(responseString, feedEntryExp, 2, "number of entry start 1 for ipad: ");

        queryMap = new HashMap<String, String>();
        queryMap.put("start", "1");
        queryMap.put("epubName", deviceType);
        queryMap.put("toDate", toDateStr);
        queryMap.put("draft", "true");
        clientResponse = getRequestCheck(queryMap, MediaType.APPLICATION_ATOM_XML, issueSearchLink, true, 200,
                "Issuu Search list test #4", false, false);
        responseString = clientResponse.getEntity(String.class);
        assertNotNull(responseString);
        getNodeListCountCheck(responseString, feedEntryExp, 1, "number of entry limit default: ");

        queryMap = new HashMap<String, String>();
        queryMap.put("start", "2");
        queryMap.put("limit", "-8");
        queryMap.put("epubName", deviceType);
        queryMap.put("toDate", toDateStr);
        queryMap.put("draft", "true");
        getRequestCheck(queryMap, MediaType.APPLICATION_ATOM_XML, issueSearchLink, true, 400,
                "Issuu Search list test #5 Bad request 400: ", false, false);

        queryMap = new HashMap<String, String>();
        queryMap.put("start", "40");
        queryMap.put("epubName", deviceType);
        queryMap.put("toDate", toDateStr);
        queryMap.put("draft", "true");
        clientResponse = getRequestCheck(queryMap, MediaType.APPLICATION_ATOM_XML, issueSearchLink, true, 200,
                "Issuu Search list test #6 200 expected", false, false);
        responseString = clientResponse.getEntity(String.class);
        assertNotNull(responseString);

        getNodeListCountCheck(responseString, feedEntryExp, 0, "number of entry for problematic start and limit: ");
        getNodeListCountCheck(responseString, "feed/link", 2,
                "number f links for start = 40 exceeding total number of files: ");

    }

    private void uploadRxmlFiles() throws IOException, XPathExpressionException {
        Path directoryPath = Paths.get("src", "test", "resources", "rxml");

        File fileToUpload = new File(directoryPath.toAbsolutePath().toString() + "/badreq_android.zip");
        InputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(fileToUpload);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ws = resource().uri(URI.create(uploadLink));
        ws.addFilter(new HTTPBasicAuthFilter("admin", "admin"));

        ClientResponse clientResponse = ws.type(MediaType.APPLICATION_OCTET_STREAM).post(ClientResponse.class,
                fileInputStream);
        assertEquals("Design name missmatched Bad request: ", 400, clientResponse.getStatus());

        fileToUpload = new File(directoryPath.toAbsolutePath().toString() + "/Chronicle_ipad.zip");
        fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(fileToUpload);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ws = resource().uri(URI.create(uploadLink));
        ws.addFilter(new HTTPBasicAuthFilter("admin", "admin"));
        clientResponse = ws.type(MediaType.APPLICATION_OCTET_STREAM).post(ClientResponse.class, fileInputStream);
        assertEquals(201, clientResponse.getStatus());
        String returnedString = clientResponse.getEntity(String.class);
        NodeList nodeList = getNodeListCountCheck(returnedString, "html/body/ul/li/a/@href", 4,
                "issue generated expected");

        List<String> issueIds = new ArrayList<String>();
        List<String> issueDetailLink = new ArrayList<String>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            issueIds.add(nodeList.item(i).getTextContent()
                    .substring(nodeList.item(i).getTextContent().lastIndexOf("/") + 1));
            issueDetailLink.add(nodeList.item(i).getTextContent());
        }
        getRxmlGeneratedIssueDetailTest(issueDetailLink, true);

        fileToUpload = new File(directoryPath.toAbsolutePath().toString() + "/ipad-p_ipad-mini.zip");
        fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(fileToUpload);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ws = resource().uri(URI.create(uploadLink));
        ws.addFilter(new HTTPBasicAuthFilter("admin", "admin"));
        clientResponse = ws.type(MediaType.APPLICATION_OCTET_STREAM).post(ClientResponse.class, fileInputStream);
        assertEquals(201, clientResponse.getStatus());
        returnedString = clientResponse.getEntity(String.class);
        nodeList = getNodeListCountCheck(returnedString, "html/body/ul/li/a/@href", 1, "issue generated expected");

        issueIds = new ArrayList<String>();
        issueDetailLink = new ArrayList<String>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            issueIds.add(nodeList.item(i).getTextContent()
                    .substring(nodeList.item(i).getTextContent().lastIndexOf("/") + 1));
            issueDetailLink.add(nodeList.item(i).getTextContent());
        }
        getRxmlGeneratedIssueDetailTest(issueDetailLink, true);

        fileToUpload = new File(directoryPath.toAbsolutePath().toString() + "/Chronicle2_ipad.zip");
        fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(fileToUpload);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ws = resource().uri(URI.create(uploadLink));
        ws.addFilter(new HTTPBasicAuthFilter("admin", "admin"));
        clientResponse = ws.type(MediaType.APPLICATION_OCTET_STREAM).post(ClientResponse.class, fileInputStream);
        assertEquals(201, clientResponse.getStatus());
        returnedString = clientResponse.getEntity(String.class);
        nodeList = getNodeListCountCheck(returnedString, "html/body/ul/li/a/@href", 4, "issue generated expected");

        issueIds = new ArrayList<String>();
        issueDetailLink = new ArrayList<String>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            issueIds.add(nodeList.item(i).getTextContent()
                    .substring(nodeList.item(i).getTextContent().lastIndexOf("/") + 1));
            issueDetailLink.add(nodeList.item(i).getTextContent());
        }
        getRxmlGeneratedIssueDetailTest(issueDetailLink, false);

        getIssueListTest();
        String issueId = "2";
        String issueLink = resource().path(BASE_URL).path("/polaris/addressa/issues").path(issueId).toString();
        getAccessTokenTest(issueLink);
    }

    public void getRxmlGeneratedIssueDetailTest(List<String> issueDetailsLinks, boolean skipUpdateAndEventQueue)
            throws XPathExpressionException, IOException {
        String containerXmlFile = "/META-INF/container.xml";
        String cssFile = "/OPS/fontsizes.css";
        String xhtmlFile = "/OPS/nav.xhtml";
        String jsonFile = "/OPS/cciobjects.json";
        Map<String, String> issueDownloadLinkMap = new HashMap<String, String>();
        Map<String, String[]> issueEpubContentLinkMap = new HashMap<String, String[]>();
        Map<String, String> issueEventsLinkMap = new HashMap<String, String>();

        for (String issueDetailLink : issueDetailsLinks) {
            if (Strings.isNullOrEmpty(System.getProperty(SYS_AUTH_SKIP))
                    || System.getProperty(SYS_AUTH_SKIP).equals("false")) {
                String issueLink = issueDetailLink;
                String token = getAccessTokenTest(issueLink);
                String fakeIssueLink = null;

                assertTrue("Issue detail link must end with a /", issueDetailLink.endsWith("/"));

                String parseIssueId = issueDetailLink.substring(0, issueDetailLink.length() - 1);
                parseIssueId = parseIssueId.substring(parseIssueId.lastIndexOf("/") + 1);

                if (Long.parseLong(parseIssueId) == 1) {
                    fakeIssueLink = resource().path(BASE_URL).path("/polaris/addressa/issues")
                            .path(String.valueOf(Long.parseLong(parseIssueId) + 1)).toString();
                } else {
                    fakeIssueLink = resource().path(BASE_URL).path("/polaris/addressa/issues")
                            .path(String.valueOf(Long.parseLong(parseIssueId) - 1)).toString();
                }

                accesstokenCookieTest(fakeIssueLink, token, 302, MediaType.APPLICATION_XHTML_XML,
                        "Authentication Error expected: " + issueDetailLink + "_fake: " + fakeIssueLink);
            }

            String issueLink = issueDetailLink;
            String token = getAccessTokenTest(issueLink);

            invalidAccessTokenRedirectTest(token, issueLink);

            String responseString;
            ClientResponse clientResponse;
            NodeList nodeList;

            clientResponse = accesstokenCookieTest(issueDetailLink, token, 200, MediaType.APPLICATION_XHTML_XML,
                    "Rxml Generated IssueDeatil Test: ");

            Map<String, String> queryMap = new HashMap<String, String>();
            queryMap.put(AUTH_TOKEN_NAME, token);
            ws = resource().uri(URI.create(issueDetailLink)).queryParam(AUTH_TOKEN_NAME, token);
            responseString = clientResponse.getEntity(String.class);
            assertNotNull(responseString);
            xpathHelper = new XpathHelper(responseString);
            String dateString = xpathHelper.parseSingleNodeValue("html/body/dl/dd[@id='date']");
            assertNotNull("Date expected in issueDeatil page but found : " + dateString, dateString);
            try {
                Date issueDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
                assertNotNull("issue date object expected: " + dateString, issueDate);
            } catch (ParseException e) {
                assertFalse("Exception happened : " + e.getMessage(), true);
                throw new RuntimeException(e.getMessage());
            }
            getNodeListCountCheck(responseString, "html/body/dl/dd/a", 6, "expected links : " + "for "
                    + issueDetailLink);

            clientResponse.getEntityTag().getValue();

            nodeList = getNodeListCountCheck(responseString, "html/body/dl/dd/a/@href", 6,
                    "expected hrefs in issueDetail page");

            assertEquals(issueDetailLink + "content.epub", nodeList.item(3).getTextContent());
            assertEquals(issueDetailLink + "META-INF/container.xml", nodeList.item(4)
                    .getTextContent());
            assertEquals(issueDetailLink + "events", nodeList.item(5).getTextContent());

            String issueDetailLinks[] = new String[] { issueDetailLink + containerXmlFile, issueDetailLink + cssFile,
                    issueDetailLink + xhtmlFile, issueDetailLink + jsonFile };
            issueDownloadLinkMap.put(issueDetailLink, nodeList.item(3).getTextContent());
            issueEpubContentLinkMap.put(issueDetailLink, issueDetailLinks);
            issueEventsLinkMap.put(issueDetailLink, nodeList.item(5).getTextContent());

            ws = resource().uri(URI.create(issueDetailLink));
            clientResponse = ws.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            assertEquals(406, clientResponse.getStatus());
            getRequestCheck(new HashMap<String, String>(), MediaType.APPLICATION_JSON, issueDetailLink, true, 406,
                    "Produces unsupported media type", true, true);

            accesstokenCookieTest(issueDetailLink, token, 200, MediaType.APPLICATION_XHTML_XML,
                    "200 expected RxmlGeneratedIssueDetail test");
        }

        downloadEpubTest(issueDownloadLinkMap);
        getEpubContentTest(issueEpubContentLinkMap);

        if (!skipUpdateAndEventQueue) {
            /* Chronicle2_ipad.zip is updated only for integration test */
            updateRxmlFileTest();
            /* Chronicle2_ipad.zip is updated only for integration test and event queue test */
            rxmlGeneratedEpubEventQueueTest(issueEventsLinkMap);
        }
    }

    public void updateRxmlFileTest() {
        Path directoryPath = Paths.get("src", "test", "resources", "rxmlUpdated");
        File fileToUpdate = new File(directoryPath.toAbsolutePath().toString() + "/Chronicle2_update_ipad.zip");
        InputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(fileToUpdate);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ws = resource().uri(URI.create(uploadLink));
        ws.addFilter(new HTTPBasicAuthFilter("admin", "admin"));
        ClientResponse clientResponse = ws.type(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.IF_MATCH, "\"" + "0" + "\"").post(ClientResponse.class, fileInputStream);
        assertEquals("updateRxmlFileTest http found code: ", 200, clientResponse.getStatus());

        Path rxmlDirectoryPath = Paths.get("src", "test", "resources", "rxml");
        File badreqFile = new File(rxmlDirectoryPath.toAbsolutePath().toString() + "/badreq_android.zip");
        try {
            fileInputStream = new FileInputStream(badreqFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ws = resource().uri(URI.create(uploadLink));
        ws.addFilter(new HTTPBasicAuthFilter("admin", "admin"));
        clientResponse = ws.type(MediaType.APPLICATION_OCTET_STREAM).post(ClientResponse.class, fileInputStream);
        assertEquals("Bad request expected: ", 400, clientResponse.getStatus());

        try {
            fileInputStream = new FileInputStream(fileToUpdate);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ws = resource().uri(URI.create(uploadLink));
        ws.addFilter(new HTTPBasicAuthFilter("admin", "admin"));
        clientResponse = ws.type(MediaType.APPLICATION_OCTET_STREAM).header(HttpHeaders.IF_MATCH, "\"" + "0" + "\"")
                .post(ClientResponse.class, fileInputStream);
        assertEquals("Precondition failed error found: ", 412, clientResponse.getStatus());
    }

    public void rxmlGeneratedEpubEventQueueTest(Map<String, String> issueEventsLinkMap)
            throws XPathExpressionException, UnsupportedEncodingException {
        for (Map.Entry<String, String> issueEventsLink : issueEventsLinkMap.entrySet()) {
            resource().uri(URI.create(issueEventsLink.getKey())).toString();
            String eventsLink = issueEventsLink.getValue();

            String accesstoken = URLEncoder.encode(getAccessTokenTest(eventsLink), "UTF-8");
            Cookie cookie = new Cookie(AUTH_TOKEN_NAME, accesstoken);
            ClientResponse clientResponse = accesstokenCookieTest(eventsLink, accesstoken, 200,
                    MediaType.APPLICATION_ATOM_XML, "rxmlGeneratedEpubEventQueueTest() 200 expected");
            String responseString = clientResponse.getEntity(String.class);
            assertNotNull(responseString);
            getNodeListCountCheck(responseString, "feed/entry", 8, "number of events entry: ");

            /* This links should be hard coded */
            ws = resource().uri(URI.create(eventsLink));
            clientResponse = ws.cookie(cookie).header("If-Modified-Since", "2100-10-10")
                    .accept(MediaType.APPLICATION_ATOM_XML)
                    .get(ClientResponse.class);
            assertEquals("Bad Request: ", 400, clientResponse.getStatus());

        }

        getRequestCheck(new HashMap<String, String>(), MediaType.APPLICATION_ATOM_XML,
                "/polaris/addressa/issues/123/events/", false, 404, "not found error code from events queue", false,
                false);

        getRequestCheck(new HashMap<String, String>(), MediaType.APPLICATION_XML,
                "/polaris/addressa/issues/123/events/", false, 406, "Not acceptable error code from events queue",
                false, false);
    }

    public String getAccessTokenTest(String issueLink) {
        ws = resource().uri(URI.create(accessTokenLink));
        ws.addFilter(new HTTPBasicAuthFilter("admin", "admin"));

        Form form = new Form();
        form.add("issueLink", issueLink);
        ClientResponse clientResponse = ws.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, form);
        assertEquals("Expected Http code", SC_OK, clientResponse.getStatus());

        String tokenValue = clientResponse.getEntity(String.class);
        assertEquals("AUTH token is not present.", true, !StringUtils.isBlank(tokenValue));

        clientResponse = ws.accept(MediaType.APPLICATION_XML).post(ClientResponse.class, form);
        assertEquals("Not acceptable error code: ", 406, clientResponse.getStatus());

        return tokenValue;
    }

    private ClientResponse accesstokenCookieTest(
            String requestUri,
            String accessToken,
            int statusCode,
            String mediaType,
            String message) {
        // First we test with the auth token in the URL
        ws = resource().uri(URI.create(requestUri)).queryParam(AUTH_TOKEN_NAME, accessToken);
        ClientResponse clientResponse = ws.get(ClientResponse.class);
        assertEquals("Unexpected status code " + ws, statusCode, clientResponse.getStatus());

        // We check if the cookie is set
        Cookie accesstokenCookie = null;
        for (NewCookie cookie : clientResponse.getCookies()) {
            if (cookie.getName().equals(AUTH_TOKEN_NAME)) {
                accesstokenCookie = cookie;
            }
        }

        if (statusCode == SC_OK) {
            assertNotNull("accesstoken Cookie expected here : ", accesstokenCookie);
        }

        // Second, we test with a auth cookie
        accesstokenCookie = new NewCookie(AUTH_TOKEN_NAME, accessToken);
        ws = resource().uri(URI.create(requestUri));
        clientResponse = ws.accept(mediaType).cookie(accesstokenCookie).get(ClientResponse.class);
        assertEquals(message, statusCode, clientResponse.getStatus());

        return clientResponse;
    }

    private void invalidAccessTokenRedirectTest(String token, String issueLink) {
        AccessToken validToken = AccessToken.from(token);

        AccessToken dummyToken = new AccessToken(SECRET_KEY, 0, validToken.getProductId());
        String signature = Hashing.md5().hashString(dummyToken.toString(), Charsets.UTF_8).toString();
        AccessToken invalidToken = new AccessToken(signature, 0, validToken.getProductId());
        Cookie expiredToken = new Cookie(AUTH_TOKEN_NAME, invalidToken.toString());

        checkInvalidToken(issueLink, 302, expiredToken);
        checkInvalidToken(issueLink, 302, new Cookie(AUTH_TOKEN_NAME, ""));
        checkInvalidToken(issueLink, 302, new Cookie(AUTH_TOKEN_NAME, "Obviously_wrong_value"));
        checkInvalidToken(issueLink, 302, new Cookie(AUTH_TOKEN_NAME, "wrongWrongWrong|1386172372|addressa:1"));
    }

    private void checkInvalidToken(String issueLink, int expectedStatusCode, Cookie tokenCookie) {
        ws = resource().uri(URI.create(issueLink));
        ClientResponse clientResponse = ws.cookie(tokenCookie).get(ClientResponse.class);
        assertEquals(expectedStatusCode + " expected for invalid token: ",
                expectedStatusCode,
                clientResponse.getStatus());

        if (expectedStatusCode >= 300 && expectedStatusCode < 400) {
            // Check location header for redirects only.
            URI locationURI = clientResponse.getLocation();
            assertNotNull("Location header expected: ", locationURI);
            assertEquals("Checking the locationURI value",
                    locationURI.toString(),
                    redirectURLWithQueryKey + StringUtils.urlEncode(issueLink));
        }
    }
}
