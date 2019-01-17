package com.cefalo.cci.dao;

import static com.cefalo.cci.utils.StringUtils.createCsv;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import com.cefalo.cci.enums.IssueStatus;
import com.cefalo.cci.enums.SortBy;
import com.cefalo.cci.model.DriverInfo;
import com.cefalo.cci.model.Issue;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.model.RxmlZipFile;
import com.google.inject.persist.Transactional;

public class IssueDaoImpl extends EntityManagerDao implements IssueDao {

    @Override
    public long getIssueCountByPublicationId(String publicationId) {
        return (Long) getEntityManager()
                .createQuery("select count(i) from Issue i where i.publication.id like :pName")
                .setParameter("pName", publicationId)
                .getSingleResult();
    }

    @Override
    public long getIssueCountByPublicationAndDeviceId(
            String publicationId, 
            List<Long> deviceTypeIds, 
            List<IssueStatus> statusList,
            Date toDate) {
        String sql = "select count(i) from Issue i where i.publication.id like :pName and i.driverInfo.designToEpubMapper.id in("
                + createCsv(deviceTypeIds) + ") and i.status in ("
                + createCsv(statusList) + ") ";
        if (toDate != null) {
            sql += " and i.created <= :toDate ";
        }
        Query query = getEntityManager().createQuery(sql)
                .setHint("org.hibernate.cacheable", true)
                .setParameter("pName", publicationId);

        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }
        return (Long) query.getSingleResult();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Issue> getIssuesOfPublication(Publication publication) {
        return getEntityManager()
                .createQuery(
                        "select i from Issue i where "
                                + "i.publication.id like :pName "
                                + "order by i.updated desc")
                .setParameter("pName", publication.getId())
                .getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Issue> getIssueListByPublicationId(String publicationId, long start, long maxResult) {
        return getEntityManager()
                .createQuery("select i from Issue i where i.publication.id like :pName order by i.updated  desc")
                .setParameter("pName", publicationId).setFirstResult((int) start).setMaxResults((int) maxResult)
                .getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Issue> getIssueListByPublicationAndDeviceId(
            String publicationId, 
            long start, long maxResult,
            List<Long> deviceTypeIds, 
            List<IssueStatus> statusList, 
            String sortOrder, String sortBy, 
            Date toDate) {
        String sql = "select i from Issue i where i.publication.id like :pName";
        if (!deviceTypeIds.isEmpty()) {
            sql += " and i.driverInfo.designToEpubMapper.id in(" + createCsv(deviceTypeIds) + ")";
        }
        if (!statusList.isEmpty()) {
            sql += " and i.status in (" + createCsv(statusList) + ")";
        }
        if (toDate != null) {
            sql += " and i.created <= :toDate ";
        }
        if (sortBy.toLowerCase().equals(SortBy.CREATED.getValue())) {
            sql += "order by i.created " + sortOrder;
        } else if (sortBy.toLowerCase().equals(SortBy.UPDATED.getValue())) {
            sql += "order by i.updated " + sortOrder;
        }
        Query query = getEntityManager().createQuery(sql)
                .setHint("org.hibernate.cacheable", true)
                .setParameter("pName", publicationId);
        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }
        return query.setFirstResult((int) start).setMaxResults((int) maxResult).getResultList();
    }

    @Override
    public Issue getIssue(Long id) {
        return getEntityManager().find(Issue.class, id);
    }

    @Override
    public Issue getIssueByZipAndDriverId(long zipFileId, long driverInfoId) {
        @SuppressWarnings("unchecked")
        List<Issue> results = getEntityManager()
                .createQuery(
                        "select i from Issue i where "
                                + "i.rxmlZipFile.id = :zipFileId and "
                                + "i.driverInfo.id = :driverInfoId")
                .setParameter("zipFileId", zipFileId)
                .setParameter("driverInfoId", driverInfoId)
                .getResultList();
        if (results.size() > 1) {
            throw new RuntimeException(String.format("There should be one Issue for zipFileId: %s, designMapperId: %s",
                    zipFileId, driverInfoId));
        }
        return results.size() == 1 ? results.get(0) : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Issue> getOldIssueList(Date date) {
        return getEntityManager()
                .createQuery("select i from Issue i where i.updated < :date")
                .setParameter("date", date)
                .getResultList();
    }

    @Override
    @Transactional
    public Issue createIssue(
            Publication publication,
            String fileName,
            DriverInfo driverInfo,
            RxmlZipFile rxmlFile,
            int issueStatus,
            Date createDate) {
        Issue issue = createIssueObject(publication, fileName, driverInfo, rxmlFile, issueStatus, createDate);
        getEntityManager().persist(issue);
        return issue;
    }

    private Issue createIssueObject(
            Publication publication,
            String fileName,
            DriverInfo driverInfo,
            RxmlZipFile rxmlFile,
            int issueStatus,
            Date createDate) {
        Issue issue = new Issue();
        issue.setName(fileName);
        issue.setCreated(createDate);
        issue.setUpdated(createDate);
        issue.setPublication(publication);
        issue.setStatus(issueStatus);
        // When the issue is first created, it is "Stale" since the EPUB is not there yet.
        issue.setStale(true);
        issue.setDriverInfo(driverInfo);
        issue.setRxmlZipFile(rxmlFile);
        return issue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Issue> getIssueListByZipFileId(long zipFileId) {
        return getEntityManager()
                .createQuery("select i from Issue i where i.rxmlZipFile.id = :zipFileId")
                .setParameter("zipFileId", zipFileId)
                .getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Issue> getPreGenIssueListByZipFileId(long zipFileId) {
        return getEntityManager()
                .createQuery(
                        "select i from Issue i, EpubFile e where "
                                + "i.rxmlZipFile.id = :zipFileId and "
                                + "i.id = e.issue.id")
                .setParameter("zipFileId", zipFileId)
                .getResultList();
    }

    @Override
    @Transactional
    public void deleteIssue(Issue issue) {
        getEntityManager().remove(issue);
    }

    @Override
    @Transactional
    public void updateIssue(Issue issue) {
        getEntityManager().persist(issue);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Issue> getIssueByDirverInfo(DriverInfo driverInfo) {
        return getEntityManager()
                .createQuery(
                        "select i from Issue i where "
                                + "i.driverInfo.id = :drvierInfoId and "
                                + "i.publication.id = :publicationId")
                .setParameter("drvierInfoId", driverInfo.getId())
                .setParameter("publicationId", driverInfo.getPublication().getId())
                .getResultList();
    }
}
