package com.cefalo.cci.service;

import com.cefalo.cci.model.DriverInfo;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.model.RxmlZipFile;

import java.util.List;

public interface RxmlService {
    RxmlZipFile getRxmlFileByName(String fileName, String publicationId);

    RxmlZipFile getRxmlById(long rxml_file_id);

    void createRxmlFile(RxmlZipFile rxmlZipFile);

    void updateRxmlZipFile(RxmlZipFile rxmlZipFile);

    void delete(RxmlZipFile rxmlFile);

    List<RxmlZipFile> getUniqueRxmlFilesByDesignName(DriverInfo driverInfo);

    List<RxmlZipFile> getAllRxmlFiles(Publication publication);
}
