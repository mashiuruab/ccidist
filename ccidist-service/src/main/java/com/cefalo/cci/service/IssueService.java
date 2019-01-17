package com.cefalo.cci.service;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.cefalo.cci.enums.IssueStatus;
import com.cefalo.cci.model.DriverInfo;
import com.cefalo.cci.model.Issue;
import com.cefalo.cci.model.Organization;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.model.RxmlZipFile;
import com.cefalo.cci.model.SectionImage;
import com.sun.syndication.feed.synd.SyndFeed;

public interface IssueService {
    public static final String CONTAINER_RELATIVE_PATH = "META-INF/container.xml";

    Issue createIssue(RxmlZipFile rxmlFile, DriverInfo driverInfo) throws Exception;

    void createIssueAndEpub(
            RxmlZipFile rxmlFile,
            DriverInfo driverInfo,
            File epubFile) throws Exception;

    Issue getIssue(Long issueId);

    void updateIssue(Issue issue);

    void deleteIssue(Issue issue);

    List<Issue> getIssuesOfPublication(Publication publication);

    List<Issue> getOldIssueList(Date date);

    SyndFeed getIssuesAsAtomFeed(
            Organization organization,
            Publication publication,
            long start, long limit,
            String epubName, List<Long> epubNameIds,
            String toDateStr, Date toDate,
            String sortOrder, String sortBy,
            boolean draft);

    void createRxmlFileWithIssues(RxmlZipFile rxmlZipFile, File rxmlFileOnDisk) throws Exception;

    void updateRXMlFileWithIssues(RxmlZipFile rxmlFile, File rxmlFileOnDisk) throws Exception;

    List<Issue> getIssueListByZipFileId(long zipFileId);

    List<SectionImage> getSectionImageLinks(Issue issue) throws Exception;

    String getCoverImageLink(Issue issue) throws Exception;

    List<Issue> getAdminIssueListBySearchQuery(
            String publicationId,
            int start, int limit,
            String deviceType,
            List<IssueStatus> statusList,
            Date startDate);

    void updateStatusAndDeleteIssue(Map<Long, IssueStatus> issueIdStatusMap, List<Long> issueIdToBeDeleted);

    boolean isEpubOutdated(Issue issue) throws IOException;

    void generateEpubIfNecessary(Issue issue) throws IOException;

    List<Issue> getIssueByDriverInfo(DriverInfo driverInfo);

}
