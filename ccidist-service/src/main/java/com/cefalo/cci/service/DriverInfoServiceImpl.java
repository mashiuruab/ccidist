package com.cefalo.cci.service;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cefalo.cci.dao.DriverDao;
import com.cefalo.cci.enums.IssueStatus;
import com.cefalo.cci.event.manager.EventManager;
import com.cefalo.cci.event.model.Event;
import com.cefalo.cci.event.model.EventType;
import com.cefalo.cci.model.DesignToEpubMapper;
import com.cefalo.cci.model.DriverInfo;
import com.cefalo.cci.model.Issue;
import com.cefalo.cci.model.RxmlZipFile;
import com.cefalo.cci.storage.Storage;
import com.cefalo.cci.utils.DateUtils;
import com.google.common.collect.Lists;
import com.google.inject.persist.Transactional;

public class DriverInfoServiceImpl implements DriverInfoService {
    private final Logger logger = LoggerFactory.getLogger(DriverInfoServiceImpl.class);

    @Inject
    private DriverDao driverDao;

    @Inject
    private IssueService issueService;

    @Inject
    private Storage<Issue> epubStorage;

    @Inject
    private RxmlService rxmlService;

    @Inject
    private EventManager eventManager;

    @Override
    public List<DriverInfo> getDrivers(String publicationId) {
        return driverDao.getDrivers(publicationId);
    }

    @Override
    public List<DriverInfo> getDrivers(String publicationId, String desingName, Date createDate) {
        return driverDao.getDrivers(publicationId, desingName, createDate);
    }

    @Override
    public List<DriverInfo> getDrivers(String publicationId, String desingName, Date createDate, boolean preGenerate) {
        return driverDao.getDrivers(publicationId, desingName, createDate, preGenerate);
    }

    @Override
    public List<DesignToEpubMapper> getDesignByPublicationId(String desingName, String publicationId, Date createDate) {
        return driverDao.getDesignByPublicationId(publicationId, desingName, createDate);
    }

    @Override
    public List<String> getUniqueEpubNames(String publicationId) {
        List<DesignToEpubMapper> designToEpubMapperList = driverDao
                .getAllDesignToEpubMapperByPublicationId(publicationId);
        Set<String> deviceTypeSet = new HashSet<String>();
        for (DesignToEpubMapper designToEpubMapper : designToEpubMapperList) {
            deviceTypeSet.add(designToEpubMapper.getEpubName());
        }
        return Lists.newArrayList(deviceTypeSet);
    }

    @Override
    public List<DesignToEpubMapper> getAllDesignToEpubMapper() {
        return driverDao.getAllDesignToEpubMapper();
    }

    @Override
    public DesignToEpubMapper getDesignToEpubMapper(String designName, String epubName) {
        return driverDao.getDesignToEpubMapper(designName, epubName);
    }

    @Override
    public List<DesignToEpubMapper> getAllDesignToEpubMapperByPublicationId(String publicationId) {
        return driverDao.getAllDesignToEpubMapperByPublicationId(publicationId);
    }

    @Override
    public void saveDesignMapper(DesignToEpubMapper designToEpubMapper) {
        driverDao.saveDesignMapper(designToEpubMapper);
    }

    @Override
    public DriverInfo getDriver(long driverId) {
        return driverDao.getDriver(driverId);
    }

    @Override
    @Transactional
    public void createDriverInfo(DriverInfo driverInfo) {
        driverDao.saveDriverInfo(driverInfo);

        // Now create any issues required.
        List<RxmlZipFile> rxmlZipFiles = getRelatedRxmlFiles(driverInfo);

        for (RxmlZipFile rxmlZipFile : rxmlZipFiles) {
            if (logger.isDebugEnabled()) {
                logger.debug("RxmlFile name = {} and id = {}", rxmlZipFile.getFileName(), rxmlZipFile.getId());
            }

            try {
                Issue issue = issueService.createIssue(rxmlZipFile, driverInfo);
                if (logger.isInfoEnabled()) {
                    logger.info("Created Issue: {} because of the creation of Driver rule: {}",
                            issue.getId(),
                            driverInfo.getId());
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        eventManager.post(new Event(EventType.CREATE, DriverInfo.class, driverInfo.getId(), null));
    }

    @Override
    @Transactional
    public void updateDriverInfo(DriverInfo updatedDriver, DriverInfo dbDriver) {
        DriverInfo expiredLockedDriver = null;

        String newDesignName = updatedDriver.getDesignToEpubMapper().getDesignName();
        String oldDesignName = dbDriver.getDesignToEpubMapper().getDesignName();
        boolean designNameChanged = !Objects.equals(newDesignName, oldDesignName);
        if (designNameChanged) {
            if (logger.isDebugEnabled()) {
                logger.debug("Driver design name has changed. Old Design: {}, New Design: {}",
                        oldDesignName,
                        newDesignName);
            }
            expiredLockedDriver = createExpiredLockedDriver(dbDriver);
        }

        // Save the driver
        sync(updatedDriver, dbDriver);
        driverDao.saveDriverInfo(dbDriver);

        List<Issue> issueList = issueService.getIssueByDriverInfo(dbDriver);
        for (Issue issue : issueList) {
            // If design name has not changed, then we just delete the Issue binary
            if (!designNameChanged) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Setting 'STALE' flag on Issue: {} for change to DriverRule: {}",
                            issue.getId(),
                            dbDriver.getId());
                }

                // We'll just mark the issue as stale. That'll re-generate the issue on next request.
                issue.setStale(true);
                issueService.updateIssue(issue);
            } else {
                // Since design name has changed, we "kinda" make the issue orphan w.r.t. driver rule.
                if (logger.isDebugEnabled()) {
                    logger.debug("Setting LOCKED driver for issue: {} for change to DriverRule: {}",
                            issue.getId(),
                            dbDriver.getId());
                }

                issue.setDriverInfo(expiredLockedDriver);
                issueService.updateIssue(issue);
            }
        }

        if (designNameChanged) {
            // Since the design name has changed we need to create the new issues. This is pretty much the same as
            // creating a new driver.
            createDriverInfo(dbDriver);
        }

        eventManager.post(new Event(EventType.UPDATE, DriverInfo.class, dbDriver.getId(), null));
    }

    @Override
    @Transactional
    public void deleteDriver(long driverId) {
        DriverInfo driver = driverDao.getDriver(driverId);
        String publicationId = driver.getPublication().getId();

        // Update the driver to an expired/locked thing.
        driver.setInternal(true);
        driver.setEndDate(DateUtils.convertDateWithTZ(new Date(TimeUnit.DAYS.toMillis(3))));
        driver.setPreGenerate(false);
        driverDao.saveDriverInfo(driver);

        boolean driverCanBeDeleted = true;
        List<Issue> issueList = issueService.getIssueByDriverInfo(driver);
        for (Issue issue : issueList) {
            if (safeToDelete(issue)) {
                // The issue is draft. We can safely delete this.
                issueService.deleteIssue(issue);
            } else {
                // We can't delete this issue. We'll just sever the ties with the Driver.
                issue.setDriverInfo(driver);
                issueService.updateIssue(issue);

                driverCanBeDeleted = false;
            }
        }

        if (driverCanBeDeleted) {
            driverDao.deleteDriver(driverId);
        }

        eventManager.post(new Event(EventType.DELETE, DriverInfo.class, driverId, publicationId));
    }

    /**
     * Returns <code>true</code> if issue is in DRAFT state or the EPUB does not exist. If there is no EPUB binary, we
     * can be sure that the EPUB has never been generated, i.e. this issue has never been requested by a user.
     *
     * @param issue
     * @return <code>true</code> if it is safe to delete the issue, <code>false</code> otherwise.
     */
    private boolean safeToDelete(Issue issue) {
        try {
            return issue.getStatus() == IssueStatus.DRAFT.getValue() || !epubStorage.exists(issue);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private List<RxmlZipFile> getRelatedRxmlFiles(DriverInfo driverInfo) {
        List<RxmlZipFile> rxmlZipFiles = rxmlService.getUniqueRxmlFilesByDesignName(driverInfo);
        return rxmlZipFiles;
    }

    private DriverInfo createExpiredLockedDriver(DriverInfo driverInfo) {
        DriverInfo expLockedDriver = new DriverInfo();

        sync(driverInfo, expLockedDriver);

        // Mark this as expired in way past. Basically 3 days since epoch. Why 3? I don't know ;-)
        Date past = DateUtils.convertDateWithTZ(new Date(TimeUnit.DAYS.toMillis(3)));
        expLockedDriver.setEndDate(past);
        expLockedDriver.setUpdated(past);
        // It really doesn't matter. But lets say pregen = false.
        expLockedDriver.setPreGenerate(false);
        // Mark this as a tricky driver :-)
        expLockedDriver.setInternal(true);

        // Persist the driver
        driverDao.saveDriverInfo(expLockedDriver);

        return expLockedDriver;
    }

    private void sync(DriverInfo src, DriverInfo dest) {
        dest.setCreated(src.getCreated());
        dest.setDesignToEpubMapper(src.getDesignToEpubMapper());
        dest.setDeviceName(src.getDeviceName());
        dest.setEndDate(src.getEndDate());
        dest.setInternal(src.isInternal());
        dest.setOs(src.getOs());
        dest.setOsVersion(src.getOsVersion());
        dest.setPreGenerate(src.isPreGenerate());
        dest.setPublication(src.getPublication());
        dest.setReader(src.getReader());
        dest.setStartDate(src.getStartDate());
        dest.setUpdated(src.getUpdated());

        // We don't sync the version & id fields.
    }
}
