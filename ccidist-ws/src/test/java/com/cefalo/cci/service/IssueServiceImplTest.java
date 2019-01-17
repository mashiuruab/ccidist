package com.cefalo.cci.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.cefalo.cci.utils.locator.ResourceLocator;
import com.cefalo.cci.model.Issue;
import com.cefalo.cci.model.Organization;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.unitTest.GuiceJUnitRunner;
import com.cefalo.cci.utils.AtomUtils;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndLink;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({ InjectModule.class })
public class IssueServiceImplTest {

    private IssueServiceImpl issueServiceImpl;

    @Before
    public void beforeTest() {
        issueServiceImpl = new IssueServiceImpl();
    }

    @Test
    public void getLinksTest() {
        DateTime toDate = new DateTime().withTimeAtStartOfDay().withDayOfMonth(1).withYear(2012);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        String toDateStr = fmt.print(toDate);
        String sortOrder = "desc";
        String sortBy = "created";
        String draft = "false";

        List<String> expectedAllRelList = new ArrayList<String>();
        expectedAllRelList.add("self");
        expectedAllRelList.add("prev");
        expectedAllRelList.add("next");

        List<String> selfPrevList = new ArrayList<String>();
        selfPrevList.add("self");
        selfPrevList.add("prev");

        List<String> selfNextList = new ArrayList<String>();
        selfNextList.add("self");
        selfNextList.add("next");

        String issueListUri = "/webservice/polaris/addressa/issue";

        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("epubName", "ipad");
        queryParams.put("sortOrder", sortOrder);
        queryParams.put("sortBy", sortBy);
        queryParams.put("draft", draft);
        queryParams.put("toDate", toDateStr);

        List<SyndLink> syndLinkList = AtomUtils.getLinks(1, 5, 12, issueListUri, queryParams);
        assertEquals(2, syndLinkList.size());
        assertEquals("self: start=1&limit=5&epubName=ipad&sortOrder=desc&sortBy=created&draft=false&toDate="
                + toDateStr, issueListUri
                + "?start=1&limit=5&epubName=ipad&sortOrder=desc&sortBy=created&draft=false&toDate=" + toDateStr,
                syndLinkList.get(0).getHref());
        assertEquals("next: start=6&limit=5&epubName=ipad&sortOrder=desc&sortBy=created&draft=false&toDate="
                + toDateStr, issueListUri
                + "?start=6&limit=5&epubName=ipad&sortOrder=desc&sortBy=created&draft=false&toDate=" + toDateStr,
                syndLinkList.get(1).getHref());

        List<String> actualList = new ArrayList<String>();
        for (SyndLink aSyndLinkList : syndLinkList) {
            actualList.add(aSyndLinkList.getRel());
        }
        assertEquals("relation by relation check: self next", selfNextList, actualList);

        syndLinkList = AtomUtils.getLinks(2, 5, 12, issueListUri, queryParams);
        assertEquals(3, syndLinkList.size());
        assertEquals("self: start=2&limit=5&epubName=ipad&sortOrder=desc&sortBy=created&draft=false&toDate="
                + toDateStr, issueListUri
                + "?start=2&limit=5&epubName=ipad&sortOrder=desc&sortBy=created&draft=false&toDate=" + toDateStr,
                syndLinkList.get(0).getHref());
        assertEquals("prev: start=1&limit=5&epubName=ipad&sortOrder=desc&sortBy=created&draft=false&toDate="
                + toDateStr, issueListUri
                + "?start=1&limit=5&epubName=ipad&sortOrder=desc&sortBy=created&draft=false&toDate=" + toDateStr,
                syndLinkList.get(1).getHref());
        assertEquals("next: start=7&limit=5&epubName=ipad&sortOrder=desc&sortBy=created&draft=false&toDate="
                + toDateStr, issueListUri
                + "?start=7&limit=5&epubName=ipad&sortOrder=desc&sortBy=created&draft=false&toDate=" + toDateStr,
                syndLinkList.get(2).getHref());

        actualList = new ArrayList<String>();
        for (SyndLink aSyndLinkList : syndLinkList) {
            actualList.add(aSyndLinkList.getRel());
        }

        assertEquals("relation by relation check: self prev next", expectedAllRelList, actualList);

        syndLinkList = AtomUtils.getLinks(7, 5, 12, issueListUri, queryParams);
        assertEquals(3, syndLinkList.size());
        assertEquals("self: start=7&limit=5&epubName=ipad&sortOrder=desc&sortBy=created&draft=false&toDate="
                + toDateStr, issueListUri
                + "?start=7&limit=5&epubName=ipad&sortOrder=desc&sortBy=created&draft=false&toDate=" + toDateStr,
                syndLinkList.get(0).getHref());
        assertEquals("prev: start=2&limit=5&epubName=ipad&sortOrder=desc&sortBy=created&draft=false&toDate="
                + toDateStr, issueListUri
                + "?start=2&limit=5&epubName=ipad&sortOrder=desc&sortBy=created&draft=false&toDate=" + toDateStr,
                syndLinkList.get(1).getHref());
        assertEquals("next: start=12&limit=5&epubName=ipad&sortOrder=desc&sortBy=created&draft=false&toDate="
                + toDateStr, issueListUri
                + "?start=12&limit=5&epubName=ipad&sortOrder=desc&sortBy=created&draft=false&toDate=" + toDateStr,
                syndLinkList.get(2).getHref());

        actualList = new ArrayList<String>();

        for (SyndLink aSyndLinkList : syndLinkList) {
            actualList.add(aSyndLinkList.getRel());
        }

        assertEquals("relation by relation check: self prev next", expectedAllRelList, actualList);

        syndLinkList = AtomUtils.getLinks(7, 6, 12, issueListUri, queryParams);
        assertEquals(2, syndLinkList.size());
        assertEquals("self: start=7&limit=6&epubName=ipad&sortOrder=desc&sortBy=created&draft=false&toDate="
                + toDateStr, issueListUri
                + "?start=7&limit=6&epubName=ipad&sortOrder=desc&sortBy=created&draft=false&toDate=" + toDateStr,
                syndLinkList.get(0).getHref());
        assertEquals("prev: start=1&limit=6&epubName=ipad&sortOrder=desc&sortBy=created&draft=false&toDate="
                + toDateStr, issueListUri
                + "?start=1&limit=6&epubName=ipad&sortOrder=desc&sortBy=created&draft=false&toDate=" + toDateStr,
                syndLinkList.get(1).getHref());

        actualList = new ArrayList<String>();

        for (SyndLink aSyndLinkList : syndLinkList) {
            actualList.add(aSyndLinkList.getRel());
        }

        assertEquals("relation by relation check: self prev", selfPrevList, actualList);
    }

    @Test
    public void getIssueAsAtomFeedTest() {
        testAtomFeedHelper(5, 5, 10, 3, 5);
        testAtomFeedHelper(12, 4, 15, 2, 4);
        testAtomFeedHelper(1, 5, 0, 1, 0);
        testAtomFeedHelper(5, 5, 15, 3, 5);
        testAtomFeedHelper(15, 15, 30, 3, 15);
        testAtomFeedHelper(1, 12, 4, 1, 4);
    }

    public void testAtomFeedHelper(int start, int limit, int numberOfIssues, int expectedLinkCount,
            int expectedEntryCount) {
        assertFalse("start or limit might not be negative: ", start < 0 || limit < 0);
        String organizationId = "polaris";
        String publicationId = "addressa";
        String deviceType = "ipad";
        String sortOrder = "desc";
        String sortBy = "created";
        String draft = "true";
        DateTime fromDate = new DateTime().withTimeAtStartOfDay().withDayOfMonth(1).withYear(2012);

        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        String fromDateStr = fmt.print(new DateTime(fromDate));

        Organization organization = new Organization();
        organization.setId(organizationId);
        organization.setName(organizationId);

        Publication publication = new Publication();
        publication.setId(publicationId);
        publication.setName(publicationId);

        List<Issue> dummyIssueList = getDummyList(numberOfIssues);

        final ResourceLocator mockResourceLocator = mock(ResourceLocator.class);
        for (int i = 0; i < numberOfIssues; i++) {
            when(
                    mockResourceLocator.getIssueURI(
                            organization.getId(),
                            publication.getId(),
                            dummyIssueList.get(i).getId()))
                    .thenReturn(
                            URI.create("/" + organizationId + "/" + publicationId + "/" + dummyIssueList.get(i).getId()));
            when(
                    mockResourceLocator.getPublicIssueDetailURI(
                            organization.getId(),
                            publication.getId(),
                            dummyIssueList.get(i).getId()))
                    .thenReturn(
                            URI.create("/" + organizationId + "/" + publicationId + "/" + dummyIssueList.get(i).getId()));
        }
        when(mockResourceLocator.getIssueListURI(organizationId, publicationId)).thenReturn(
                URI.create("/" + organizationId + "/" + publicationId));
        issueServiceImpl.webserviceLocatorProvider = new Provider<ResourceLocator>() {
            @Override
            public ResourceLocator get() {
                return mockResourceLocator;
            }
        };

        int toIndex = 0;
        if (start + limit - 1 > numberOfIssues) {
            toIndex = numberOfIssues;
        } else {
            toIndex = start + limit - 1;
        }

        SyndFeed syndFeed = issueServiceImpl.getIssueAsAtomFeed(dummyIssueList.subList(start - 1, toIndex),
                organization, publication,
                start, limit, deviceType, null, fromDateStr, sortOrder, sortBy, draft, numberOfIssues);

        Assert.assertEquals("number of links: ", syndFeed.getLinks().size(), expectedLinkCount);
        Assert.assertEquals("number of entry: ", syndFeed.getEntries().size(), expectedEntryCount);

        List<String> actualList = new ArrayList<String>();

        for (int i = 0; i < syndFeed.getEntries().size(); i++) {
            SyndEntryImpl syndEntry = (SyndEntryImpl) syndFeed.getEntries().get(i);
            actualList.add(syndEntry.getTitle());
        }

        List<Issue> expectedIssueList = dummyIssueList.subList(start - 1, toIndex);

        List<String> expectedNamesInList = new ArrayList<String>();

        for (int i = 0; i < expectedIssueList.size(); i++) {
            expectedNamesInList.add(expectedIssueList.get(i).getName());
        }

        Assert.assertEquals("element by element check: ", expectedNamesInList, actualList);
    }

    public List<Issue> getDummyList(int numberOfIssues) {
        List<Issue> dummyList = new ArrayList<Issue>();
        Issue dummyIssue;
        for (int i = 0; i < numberOfIssues; i++) {
            dummyIssue = new Issue();
            dummyIssue.setId(i);
            dummyIssue.setName("demoIssue_" + i);
            dummyIssue.setCreated(new Date(2000 + i * 10));
            dummyIssue.setUpdated(new Date(2000 + i * 10 + 1));
            dummyList.add(dummyIssue);
        }
        return dummyList;
    }

}
