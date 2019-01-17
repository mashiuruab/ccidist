package com.cefalo.cci.dao;

import com.cefalo.cci.enums.IssueStatus;
import com.cefalo.cci.model.DriverInfo;
import com.cefalo.cci.model.Issue;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.model.RxmlZipFile;

import java.util.Date;
import java.util.List;

public interface IssueDao {
    long getIssueCountByPublicationId(String publicationId);

    long getIssueCountByPublicationAndDeviceId(
            String publicationId, 
            List<Long> deviceTypeIds, 
            List<IssueStatus> statusList,
            Date toDate);

    List<Issue> getIssuesOfPublication(Publication publication);

    List<Issue> getIssueListByPublicationId(String publicationId, long start, long maxResult);

    List<Issue> getIssueListByPublicationAndDeviceId(
            String publicationId, 
            long start, long maxResult,
            List<Long> deviceTypeIds, 
            List<IssueStatus> statusList, 
            String sortOrder, String sortBy, 
            Date toDate);

    Issue getIssue(Long id);

    Issue getIssueByZipAndDriverId(long zipFileId, long driverId);

    List<Issue> getOldIssueList(Date date);

    List<Issue> getIssueListByZipFileId(long zipFileId);

    List<Issue> getPreGenIssueListByZipFileId(long zipFileId);

    List<Issue> getIssueByDirverInfo(DriverInfo driverInfo);

    Issue createIssue(
            Publication publication,
            String fileName,
            DriverInfo driverInfo,
            RxmlZipFile rxmlFile,
            int issueStatus,
            Date createDate);

    void deleteIssue(Issue issue);

    void updateIssue(Issue issue);

}
