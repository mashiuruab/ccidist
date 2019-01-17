package com.cefalo.cci.dao;

import com.cefalo.cci.model.DesignToEpubMapper;
import com.cefalo.cci.model.DriverInfo;
import com.google.inject.persist.Transactional;

import java.util.Date;
import java.util.List;

public class DriverDaoImpl extends EntityManagerDao implements DriverDao {
    @Override
    @SuppressWarnings("unchecked")
    public List<DesignToEpubMapper> getAllDesignToEpubMapper() {
        return getEntityManager()
                .createQuery("Select d from DesignToEpubMapper d")
                .setHint("org.hibernate.cacheable", true)
                .getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DesignToEpubMapper> getAllDesignToEpubMapperByPublicationId(String publicationId) {
        return getEntityManager()
                .createQuery(
                        "select dr.designToEpubMapper from DriverInfo dr where "
                                + "dr.publication.id like :publicationId and "
                                + "dr.internal = false")
                .setParameter("publicationId", publicationId)
                .setHint("org.hibernate.cacheable", true)
                .getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Long> getDesignToEpubMapperIds(String publicationId, String epubName) {
        return getEntityManager()
                .createQuery(
                        "select d.id from DesignToEpubMapper d, DriverInfo df where "
                                + "d.id = df.designToEpubMapper.id and "
                                + "df.publication.id like :publicationId and "
                                + "d.epubName like :epubName and "
                                + "df.internal = false")
                .setParameter("epubName", epubName)
                .setParameter("publicationId", publicationId)
                .setHint("org.hibernate.cacheable", true)
                .getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DesignToEpubMapper> getDesignByPublicationId(String publicationId, String desingName, Date createDate) {
        return getEntityManager()
                .createQuery(
                        "select di.designToEpubMapper from DriverInfo di where "
                                + "di.publication.id like :publicationId and  "
                                + "di.designToEpubMapper.designName like :designName and "
                                + "(di.startDate is null or di.startDate <= :createDate) and "
                                + "(di.endDate is null or di.endDate >= :createDate) and "
                                + "di.internal = false")
                .setParameter("publicationId", publicationId)
                .setParameter("designName", desingName)
                .setParameter("createDate", createDate)
                .setParameter("createDate", createDate)
                .getResultList();
    }

    @Override
    public DesignToEpubMapper getDesignToEpubMapper(String designName, String epubName) {
        @SuppressWarnings("unchecked")
        List<DesignToEpubMapper> designToEpubMapperList = getEntityManager()
                .createQuery(
                        "Select d from DesignToEpubMapper d where d.designName like :designName and d.epubName like :epubName")
                .setParameter("designName", designName)
                .setParameter("epubName", epubName)
                .getResultList();

        if (designToEpubMapperList.size() > 1) {
            throw new RuntimeException(String.format(
                    "There should be a single DesignToEpubMapper for designName: %s, epubName: %s", designName,
                    epubName));
        }

        return designToEpubMapperList.size() == 1 ? designToEpubMapperList.get(0) : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DriverInfo> getDrivers(String publicationId) {
        return getEntityManager()
                .createQuery(
                        "select dr from DriverInfo dr where "
                                + "dr.publication.id like :publicationId and "
                                + "dr.internal = false")
                .setParameter("publicationId", publicationId)
                .getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DriverInfo> getDrivers(String publicationId, String designName, Date createDate) {
        return getEntityManager()
                .createQuery(
                        "select dr from DriverInfo dr "
                                + "where dr.publication.id like :publicationId and "
                                + "dr.designToEpubMapper.designName like :designName and "
                                + "(dr.startDate is null or dr.startDate <= :createDate) and "
                                + "(dr.endDate is null or dr.endDate >= :createDate) and "
                                + "dr.internal = false")
                .setParameter("publicationId", publicationId)
                .setParameter("designName", designName)
                .setParameter("createDate", createDate)
                .getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DriverInfo> getDrivers(String publicationId, String designName, Date createDate, boolean preGenerate) {
        return getEntityManager()
                .createQuery(
                        "select dr from DriverInfo dr "
                                + "where dr.publication.id like :publicationId and "
                                + "dr.designToEpubMapper.designName like :designName and "
                                + "(dr.startDate is null or dr.startDate <= :createDate) and "
                                + "(dr.endDate is null or dr.endDate >= :createDate) and "
                                + "dr.preGenerate = :preGenerate and "
                                + "dr.internal = false")
                .setParameter("publicationId", publicationId)
                .setParameter("designName", designName)
                .setParameter("createDate", createDate)
                .setParameter("preGenerate", preGenerate)
                .getResultList();
    }

    @Override
    public DriverInfo getDriver(long driverId) {
        return getEntityManager().find(DriverInfo.class, driverId);
    }

    @Override
    @Transactional
    public void saveDriverInfo(DriverInfo driverInfo) {
        getEntityManager().persist(driverInfo);
    }

    @Override
    @Transactional
    public void saveDesignMapper(DesignToEpubMapper designToEpubMapper) {
        getEntityManager().persist(designToEpubMapper);
    }

    @Override
    @Transactional
    public void deleteDriver(long id) {
        DriverInfo driverInfo = getDriver(id);
        getEntityManager().remove(driverInfo);
    }
}
