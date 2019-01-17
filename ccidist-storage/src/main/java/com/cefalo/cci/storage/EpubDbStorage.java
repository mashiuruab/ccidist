package com.cefalo.cci.storage;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.SQLException;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cefalo.cci.dao.EntityManagerDao;
import com.cefalo.cci.model.EpubFile;
import com.cefalo.cci.model.Issue;
import com.cefalo.cci.storage.util.HibernateUtil;
import com.google.common.base.Stopwatch;
import com.google.common.io.Closeables;

public class EpubDbStorage extends EntityManagerDao implements Storage<Issue> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public InputStream get(Issue issue) throws IOException {
        checkNotNull(issue);

        if (logger.isTraceEnabled()) {
            logger.trace("Getting binary data for: {}", issue.getId());
        }

        EpubFile epubFile = getEupFileByIssueId(issue.getId());
        if (epubFile == null || epubFile.getFileContent() == null) {
            return null;
        }
        try {
            return epubFile.getFileContent().getBinaryStream();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean exists(Issue issue) throws IOException {
        checkNotNull(issue);

        try {
            Object result = getEntityManager()
                    .createQuery(
                            "select e.id from EpubFile e where e.issue.id like :issueID and e.fileContent is not null")
                    .setParameter("issueID", issue.getId())
                    .setHint("org.hibernate.cacheable", true)
                    .getSingleResult();
            return result != null;
        } catch (NoResultException | NonUniqueResultException ex) {
            return false;
        }
    }

    @Override
    public InputStream getFragment(Issue issue, URI fragmentPath) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void create(Issue issue, InputStream inputStream) throws IOException {
        checkNotNull(issue);
        checkNotNull(inputStream);

        if (exists(issue)) {
            throw new IllegalArgumentException("EPUB already exists for: " + issue.getId());
        }

        createEpub(issue, inputStream);
    }

    @Override
    public void update(Issue issue, InputStream inputStream) throws IOException {
        checkNotNull(issue);
        checkNotNull(inputStream);

        // Simple JPA update would try to read in the binary into memory. This actually is faster and more efficient.
        // The only downside is that the auto_increment ID increases one more. I can live with that :-)
        delete(issue);
        createEpub(issue, inputStream);
    }

    @Override
    public void delete(Issue issue) throws IOException {
        checkNotNull(issue);

        Stopwatch timer = new Stopwatch().start();

        // By using this query, we ensure that we don't load up the BIG blob in memory.
        Query query = getEntityManager()
                .createQuery("delete from EpubFile e where e.issue.id like :issueId")
                .setParameter("issueId", issue.getId());
        query.executeUpdate();

        if (logger.isDebugEnabled()) {
            logger.info("Time to delete binary of Issue {}: {}", issue.getId(), timer.stop());
        }
    }

    @Override
    public void cleanUp(Issue issue) {
        /*
         * Intentionally left empty because DbStorage does not use any caching mechanism.
         */
    }

    private void createEpub(Issue issue, InputStream inputStream) throws IOException {
        boolean exception = false;
        try {
            EpubFile epubFile = new EpubFile();
            epubFile.setFileContent(HibernateUtil.getBlob(getEntityManager(), inputStream));
            epubFile.setIssue(issue);
            getEntityManager().persist(epubFile);
        } catch (Exception e) {
            exception = true;
            throw new RuntimeException(e);
        } finally {
            if (exception) {
                Closeables.close(inputStream, true);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private EpubFile getEupFileByIssueId(long issueID) {
        List<EpubFile> epubFileList = getEntityManager()
                .createQuery("select e from EpubFile e where e.issue.id = :issueID")
                .setParameter("issueID", issueID)
                .getResultList();
        if (!epubFileList.isEmpty()) {
            return epubFileList.get(0);
        }
        return null;
    }
}
