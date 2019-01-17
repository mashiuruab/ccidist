package com.cefalo.cci.service;

import com.cefalo.cci.model.DesignToEpubMapper;
import com.cefalo.cci.model.DriverInfo;

import java.util.Date;
import java.util.List;

public interface DriverInfoService {
    List<DriverInfo> getDrivers(String publicationId);

    List<DriverInfo> getDrivers(String publicationId, String desingName, Date createDate);

    List<DriverInfo> getDrivers(String publicationId, String desingName, Date createDate, boolean preGenerate);

    List<DesignToEpubMapper> getDesignByPublicationId(String desingName, String publicationId, Date createDate);

    List<DesignToEpubMapper> getAllDesignToEpubMapperByPublicationId(String publicationId);

    List<DesignToEpubMapper> getAllDesignToEpubMapper();

    DesignToEpubMapper getDesignToEpubMapper(String designName, String epubName);

    List<String> getUniqueEpubNames(String publicationId);

    void saveDesignMapper(DesignToEpubMapper designToEpubMapper);

    DriverInfo getDriver(long driverId);

    void createDriverInfo(DriverInfo driverInfo);

    void updateDriverInfo(DriverInfo updateDriverInfo, DriverInfo existingDriverInfo);

    void deleteDriver(long id);
}
