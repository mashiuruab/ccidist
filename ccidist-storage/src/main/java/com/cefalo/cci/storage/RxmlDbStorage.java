package com.cefalo.cci.storage;

import static com.google.common.base.Preconditions.checkNotNull;

import com.cefalo.cci.dao.EntityManagerDao;
import com.cefalo.cci.model.RxmlBinaryFile;
import com.cefalo.cci.model.RxmlZipFile;
import com.cefalo.cci.storage.util.HibernateUtil;
import com.google.common.base.Stopwatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.SQLException;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

public class RxmlDbStorage extends EntityManagerDao implements Storage<RxmlZipFile> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public InputStream get(RxmlZipFile rxmlFile) throws IOException {
        checkNotNull(rxmlFile);

        if (logger.isTraceEnabled()) {
            logger.trace("Retrieving RXML: {}", rxmlFile.getId());
        }

        RxmlBinaryFile binaryFile = getRxmlBinaryByRxmlId(rxmlFile.getId());
        if (binaryFile == null || binaryFile.getFileContent() == null) {
            throw new FileNotFoundException(String.format("No binary file for: %s", rxmlFile.getId()));
        }
        try {
            return binaryFile.getFileContent().getBinaryStream();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean exists(RxmlZipFile rxmlFile) throws IOException {
        checkNotNull(rxmlFile);

        try {
            Object result = getEntityManager()
                    .createQuery(
                            "select rxml.id from RxmlBinaryFile rxml where rxml.rxmlZipFile.id like :rxmlZipFileID and rxml.fileContent is not null")
                    .setParameter("rxmlZipFileID", rxmlFile.getId())
                    .getSingleResult();
            return result != null;
        } catch (NoResultException | NonUniqueResultException ex) {
            return false;
        }
    }

    @Override
    public InputStream getFragment(RxmlZipFile resource, URI fragmentPath) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void create(RxmlZipFile rxmlFile, InputStream inputStream) throws IOException {
        checkNotNull(rxmlFile);
        checkNotNull(inputStream);

        if (exists(rxmlFile)) {
            throw new IllegalArgumentException("RXML Binary already exists for: " + rxmlFile.getId());
        }

        createRxmlBinaryFile(rxmlFile, inputStream);
    }

    @Override
    public void update(RxmlZipFile rxmlFile, InputStream inputStream) throws IOException {
        checkNotNull(rxmlFile);
        checkNotNull(inputStream);

        // Simple JPA update would try to read in the binary into memory. This actually is faster and more efficient.
        // The only downside is that the auto_increment ID increases one more. I can live with that :-)
        delete(rxmlFile);
        createRxmlBinaryFile(rxmlFile, inputStream);
    }

    @Override
    public void delete(RxmlZipFile rxmlFile) throws IOException {
        checkNotNull(rxmlFile);

        Stopwatch timer = new Stopwatch().start();

        // By using this query, we ensure that we don't load up the BIG blob in memory.
        Query query = getEntityManager()
                .createQuery("delete from RxmlBinaryFile rbf where rbf.rxmlZipFile.id like :rxmlId")
                .setParameter("rxmlId", rxmlFile.getId());
        query.executeUpdate();

        if (logger.isDebugEnabled()) {
            logger.info("Time to delete binary of Rxml {}: {}", rxmlFile.getId(), timer.stop());
        }
    }

    @Override
    public void cleanUp(RxmlZipFile resource) {
        /*
         * Intentionally left empty because DbStorage does not use any caching mechanism.
         */
    }

    private void createRxmlBinaryFile(RxmlZipFile rxmlZipFile, InputStream inputStream) throws IOException {
        try {
            RxmlBinaryFile rxmlBinaryFile = new RxmlBinaryFile();
            rxmlBinaryFile.setRxmlZipFile(rxmlZipFile);
            rxmlBinaryFile.setFileContent(HibernateUtil.getBlob(getEntityManager(), inputStream));
            getEntityManager().persist(rxmlBinaryFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private RxmlBinaryFile getRxmlBinaryByRxmlId(long id) {
        List<RxmlBinaryFile> fileList = getEntityManager()
                .createQuery("select r from RxmlBinaryFile r where r.rxmlZipFile.id = :id")
                .setParameter("id", id)
                .getResultList();
        if (!fileList.isEmpty()) {
            return fileList.get(0);
        }
        return null;
    }
}
