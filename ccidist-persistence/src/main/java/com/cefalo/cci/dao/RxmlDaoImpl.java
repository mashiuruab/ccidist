package com.cefalo.cci.dao;

import com.cefalo.cci.model.DriverInfo;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.model.RxmlZipFile;
import com.google.inject.persist.Transactional;

import java.util.List;

public class RxmlDaoImpl extends EntityManagerDao implements RxmlDao {

    @Override
    @SuppressWarnings("unchecked")
    public RxmlZipFile getRxmlFileByName(String fileName, String publicationId) {
        List<RxmlZipFile> rxmlFileList = getEntityManager()
                .createQuery(
                        "select r from RxmlZipFile r where r.publication.id like :publicationId and r.fileName like :fileName")
                .setParameter("publicationId", publicationId)
                .setParameter("fileName", fileName)
                .getResultList();

        if (rxmlFileList.size() == 0) {
            return null;
        }
        return rxmlFileList.get(0);
    }

    @Override
    public RxmlZipFile getRxmlFileById(long id) {
        return getEntityManager().find(RxmlZipFile.class, id);
    }

    @Override
    @Transactional
    public void saveRxmlFile(RxmlZipFile rxmlFile) {
        getEntityManager().persist(rxmlFile);
    }

    @Override
    @Transactional
    public void delete(RxmlZipFile rxmlFile) {
        getEntityManager().remove(rxmlFile);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<RxmlZipFile> getUniqueRxmlFilesByDesignName(DriverInfo driverInfo) {
        return getEntityManager()
                .createQuery("select distinct rxml from RxmlZipFile rxml where "
                        + "rxml.publication.id = :publicationId and "
                        + "rxml.designName = :designName")
                .setParameter("designName", driverInfo.getDesignToEpubMapper().getDesignName())
                .setParameter("publicationId", driverInfo.getPublication().getId())
                .getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<RxmlZipFile> getAllRxmlFiles(Publication publication) {
        return getEntityManager()
                .createQuery("select rxml from RxmlZipFile rxml where "
                        + "rxml.publication.id = :publicationId")
                .setParameter("publicationId", publication.getId())
                .getResultList();
    }
}
