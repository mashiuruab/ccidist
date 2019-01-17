package com.cefalo.cci.dao;

import com.cefalo.cci.model.DriverInfo;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.model.RxmlZipFile;

import java.util.List;

public interface RxmlDao {

    RxmlZipFile getRxmlFileByName(String fileName, String publicationId);

    RxmlZipFile getRxmlFileById(long id);

    void saveRxmlFile(RxmlZipFile rxmlFile);

    void delete(RxmlZipFile rxmlFile);

    List<RxmlZipFile> getUniqueRxmlFilesByDesignName(DriverInfo driverInfo);

    List<RxmlZipFile> getAllRxmlFiles(Publication publication);

}
