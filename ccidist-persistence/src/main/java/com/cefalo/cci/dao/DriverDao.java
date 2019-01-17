package com.cefalo.cci.dao;

import com.cefalo.cci.model.DesignToEpubMapper;
import com.cefalo.cci.model.DriverInfo;

import java.util.Date;
import java.util.List;

public interface DriverDao {
    List<DesignToEpubMapper> getAllDesignToEpubMapper();

    List<DesignToEpubMapper> getAllDesignToEpubMapperByPublicationId(String publicationId);

    List<Long> getDesignToEpubMapperIds(String publicationId, String epubName);

    List<DesignToEpubMapper> getDesignByPublicationId(String publicationId, String desingName, Date createDate);

    DesignToEpubMapper getDesignToEpubMapper(String designName, String epubName);

    List<DriverInfo> getDrivers(String publicationId);

    List<DriverInfo> getDrivers(String publicationId, String designName, Date createDate);

    List<DriverInfo> getDrivers(String publicationId, String desingName, Date createDate, boolean preGenerate);

    DriverInfo getDriver(long driverId);

    void saveDriverInfo(DriverInfo driverInfo);

    void saveDesignMapper(DesignToEpubMapper designToEpubMapper);

    void deleteDriver(long id);
}
