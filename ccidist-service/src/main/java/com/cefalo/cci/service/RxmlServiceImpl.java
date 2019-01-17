package com.cefalo.cci.service;

import com.cefalo.cci.dao.RxmlDao;
import com.cefalo.cci.model.DriverInfo;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.model.RxmlZipFile;
import com.cefalo.cci.storage.Storage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.io.IOException;
import java.util.List;

public class RxmlServiceImpl implements RxmlService {

    @Inject
    private RxmlDao rxmlDao;

    @Inject
    Storage<RxmlZipFile> rxmlStorage;

    @Override
    public RxmlZipFile getRxmlFileByName(String fileName, String publicationId) {
        return rxmlDao.getRxmlFileByName(fileName, publicationId);
    }

    @Override
    public RxmlZipFile getRxmlById(long rxml_file_id) {
        return rxmlDao.getRxmlFileById(rxml_file_id);
    }

    @Override
    @Transactional
    public void createRxmlFile(RxmlZipFile rxmlZipFile) {
        rxmlDao.saveRxmlFile(rxmlZipFile);
    }

    @Override
    @Transactional
    public void updateRxmlZipFile(RxmlZipFile rxmlZipFile) {
        rxmlDao.saveRxmlFile(rxmlZipFile);
    }

    @Override
    @Transactional
    public void delete(RxmlZipFile rxmlFile) {
        try {
            rxmlDao.delete(rxmlFile);
            rxmlStorage.delete(rxmlFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<RxmlZipFile> getUniqueRxmlFilesByDesignName(DriverInfo driverInfo) {
        return rxmlDao.getUniqueRxmlFilesByDesignName(driverInfo);
    }

    @Override
    public List<RxmlZipFile> getAllRxmlFiles(Publication publication) {
        return rxmlDao.getAllRxmlFiles(publication);
    }
}
