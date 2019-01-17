package com.cefalo.cci.action;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.dao.DriverDao;
import com.cefalo.cci.model.DesignToEpubMapper;
import com.cefalo.cci.model.DriverInfo;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.service.DriverInfoService;
import com.cefalo.cci.service.PublicationService;
import com.cefalo.cci.service.UsersService;
import com.cefalo.cci.utils.DateUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import static com.cefalo.cci.utils.StringUtils.isBlank;

public class DriverRuleAction extends AuthenticatedActionSupport {
    private static final long serialVersionUID = -3566685085866922681L;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private DriverInfoService driverService;
    private PublicationService publicationService;

    private String publicationId;
    private String organizationId;
    private long driverInfoId = 0;
    private String os;
    private String osVersion;
    private String reader;
    private String deviceName;
    private Date startDate;
    private Date endDate;
    private boolean preGenerate = false;
    private String epubName;
    private String designName;

    private Map<String, Collection<String>> publicationEpubMapping;
    private Map<String, Collection<String>> allEpubMapping;
    private List<String> uniqueDesignNameList;

    @Inject
    public DriverRuleAction(ApplicationConfiguration config, DriverInfoService driverInfoService, DriverDao driverDao,
            PublicationService publicationService, UsersService usersService) {
        super(config);

        this.driverService = driverInfoService;
        this.publicationService = publicationService;
    }

    public boolean isHasAccess() {
        Publication publication = publicationService.getPublication(getPublicationId());
        return isSuperUser()
                || (publication != null && publication.getOrganization().getId().equals(getUserOrganizationId()));
    }

    @Override
    public String execute() {
        return SUCCESS;
    }

    @Override
    public void validate() {
        super.validate();

        if ("GET".equals(getRequestMethod())) {
            validateGetRequest();
        }
        if ("POST".equals(getRequestMethod())) {
            validatePostRequest();
        }

        /* This is for setting the title after validation error */
        if (driverInfoId == 0) {
            setPagetTitle(getText("message.create.driver"));
        } else {
            setPagetTitle(getText("message.edit.driver"));
        }

        if (publicationEpubMapping == null) {
            setDesignToEpubMapper(driverService.getAllDesignToEpubMapperByPublicationId(getPublicationId()));
        }
        if (allEpubMapping == null) {
            setAllEpubMapping(driverService.getAllDesignToEpubMapper());
        }
        setUniqueDesignNameList(Lists.newArrayList(publicationEpubMapping.keySet()));
    }

    private void validateGetRequest() {
        if (isBlank(getPublicationId())) {
            logger.error(getText("unauthorized.access"));
            addActionError(getText("unauthorized.access"));
        }
        if (!isBlank(getDesignName()) || !isBlank(getEpubName()) || !isBlank(getDeviceName())
                || !isBlank(getOs()) || !isBlank(getOsVersion()) || !isBlank(getReader())
                || getStartDate() != null || getEndDate() != null) {
            logger.error(getText("unauthorized.access"));
            addActionError(getText("unauthorized.access"));
        }
    }

    private void validatePostRequest() {
        if (isBlank(getDesignName())) {
            addFieldError("designName", getText("message.designName.required"));
        }

        if (isBlank(getEpubName())) {
            addFieldError("epubName", getText("message.epubName.required"));
        }

        if (isBlank(getPublicationId())) {
            addFieldError("publicationId", getText("message.publication.id.required"));
        }

        if (isBlank(designName)) {
            addFieldError("designName", getText("message.required"));
        }

        if (isBlank(epubName)) {
            addFieldError("epubName", getText("message.required"));
        }

        if (!isBlank(designName) && !isBlank(epubName)) {
            // Now we try to check if the design/epub combo is valid and available.
            List<DesignToEpubMapper> allMappings = driverService.getAllDesignToEpubMapper();
            setAllEpubMapping(allMappings);

            List<DesignToEpubMapper> allMappingsForPublication =
                    driverService.getAllDesignToEpubMapperByPublicationId(getPublicationId());
            setDesignToEpubMapper(allMappingsForPublication);

            // If this is a new epub name, we are fine.
            if (!isNewEpubName(allMappings)) {
                DriverInfo driverInfo = driverInfoId == 0 ? null : driverService.getDriver(driverInfoId);
                if (isAlreadyMapped(driverInfo, allMappingsForPublication, allMappings)) {
                    addActionError("This epub is already used. Please use a different epub  name.");
                }
            }
        }
    }

    private boolean isAlreadyMapped(DriverInfo driverInfo,
            List<DesignToEpubMapper> allMappingsForPublication, List<DesignToEpubMapper> allMappings) {
        if (driverInfo != null && driverInfo.getDesignToEpubMapper().getDesignName().equals(designName)
                && driverInfo.getDesignToEpubMapper().getEpubName().equals(epubName)) {
            // The current driver is using this combo. It is OK.
            return false;
        }

        // Check if we already used this mapping for this publication.
        for (DesignToEpubMapper mapper : allMappingsForPublication) {
            if (epubName.equals(mapper.getEpubName())) {
                return true;
            }
        }

        // Check if some other design name is using this epubName
        for (DesignToEpubMapper mapping : allMappings) {
            if (epubName.equals(mapping.getEpubName()) && !designName.equals(mapping.getDesignName())) {
                // This epub belongs to a different design. It can't be used.
                return true;
            }
        }

        return false;
    }

    private boolean isNewEpubName(List<DesignToEpubMapper> allMappings) {
        for (DesignToEpubMapper mapping : allMappings) {
            if (epubName.equals(mapping.getEpubName())) {
                return false;
            }
        }

        return true;
    }

    public String onLoad() {
        checkAccessPrivilege();
        loadDriverInfo();
        return "loaded";
    }

    private void loadDriverInfo() {
        if (driverInfoId == 0) {
            setPublicationId(publicationId);
            setPagetTitle(getText("message.create.driver"));
        } else {
            DriverInfo driverInfo = driverService.getDriver(driverInfoId);
            setDriverInfoId(driverInfo.getId());
            setPublicationId(driverInfo.getPublication().getId());
            setOs(driverInfo.getOs());
            setOsVersion(driverInfo.getOsVersion());
            setEpubName(driverInfo.getDesignToEpubMapper().getEpubName());
            setDesignName(driverInfo.getDesignToEpubMapper().getDesignName());
            setPreGenerate(driverInfo.isPreGenerate());
            setReader(driverInfo.getReader());
            setDeviceName(driverInfo.getDeviceName());

            if (driverInfo.getStartDate() != null) {
                setStartDate(DateUtils.convertDateWithTZ(driverInfo.getStartDate()));
            }

            if (driverInfo.getEndDate() != null) {
                setEndDate(DateUtils.convertDateWithTZ(driverInfo.getEndDate()));
            }
            setPagetTitle(getText("message.edit.driver"));
        }
    }

    /**
     * For saving driver info, we need the current state of the DriverInfo entity. Thats why we can't just load via JPA
     * & then call setters.
     *
     * @return
     */
    public String saveOrUpdate() {
        checkAccessPrivilege();

        DriverInfo submittedDriverInfo = new DriverInfo();
        submittedDriverInfo.setOs(getOs());
        submittedDriverInfo.setOsVersion(getOsVersion());
        submittedDriverInfo.setReader(getReader());
        submittedDriverInfo.setDeviceName(getDeviceName());
        submittedDriverInfo.setPreGenerate(Boolean.valueOf(getPreGenerate())); // check default 0

        if (getStartDate() != null) {
            submittedDriverInfo.setStartDate(DateUtils.convertDateWithTZ(getStartDate()));
        } else {
            submittedDriverInfo.setStartDate(null);
        }

        if (getEndDate() != null) {
            submittedDriverInfo.setEndDate(DateUtils.convertDateWithTZ(getEndDate()));
        } else {
            submittedDriverInfo.setEndDate(null);
        }

        DesignToEpubMapper designToEpubMapper = getDesignToEpubMapper(getDesignName(), getEpubName());
        if (designToEpubMapper.getId() == 0) {
            driverService.saveDesignMapper(designToEpubMapper);
        }
        submittedDriverInfo.setDesignToEpubMapper(designToEpubMapper);

        Publication publication = publicationService.getPublication(getPublicationId());
        submittedDriverInfo.setPublication(publication);

        if (driverInfoId == 0) {
            driverService.createDriverInfo(submittedDriverInfo);
        } else {
            DriverInfo dbDriver = driverService.getDriver(driverInfoId);
            driverService.updateDriverInfo(submittedDriverInfo, dbDriver);
        }

        return "redirect";
    }

    public DesignToEpubMapper getDesignToEpubMapper(String designName, String epubName) {
        DesignToEpubMapper designToEpubMapper =
                driverService.getDesignToEpubMapper(
                        designName.toLowerCase(),
                        epubName.toLowerCase());

        if (designToEpubMapper == null) {
            designToEpubMapper = new DesignToEpubMapper();
            designToEpubMapper.setDesignName(designName.toLowerCase());
            designToEpubMapper.setEpubName(epubName.toLowerCase());
            return designToEpubMapper;
        }
        return designToEpubMapper;
    }

    private void checkAccessPrivilege() {
        if (!isHasAccess()) {
            logger.error(String.format("Unauhorized access"));
            throw new RuntimeException("Unauhorized access");
        }
    }

    private String getUserOrganizationId() {
        return getLoggedInUser().getUserPrivilege().getOrganization().getId();
    }

    public String getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(String publicationId) {
        this.publicationId = publicationId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public long getDriverInfoId() {
        return driverInfoId;
    }

    public void setDriverInfoId(long driverInfoId) {
        this.driverInfoId = driverInfoId;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getReader() {
        return reader;
    }

    public void setReader(String reader) {
        this.reader = reader;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @TypeConversion(converter = "com.cefalo.cci.action.DateConverter")
    public Date getStartDate() {
        return startDate;
    }

    @TypeConversion(converter = "com.cefalo.cci.action.DateConverter")
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @TypeConversion(converter = "com.cefalo.cci.action.DateConverter")
    public Date getEndDate() {
        return endDate;
    }

    @TypeConversion(converter = "com.cefalo.cci.action.DateConverter")
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean getPreGenerate() {
        return preGenerate;
    }

    public void setPreGenerate(boolean preGenerate) {
        this.preGenerate = preGenerate;
    }

    public String getEpubName() {
        return epubName;
    }

    public void setEpubName(String epubName) {
        this.epubName = epubName;
    }

    public String getDesignName() {
        return designName;
    }

    public void setDesignName(String designName) {
        this.designName = designName;
    }

    public Map<String, Collection<String>> getPublicationEpubMapping() {
        return publicationEpubMapping;
    }

    private void setDesignToEpubMapper(List<DesignToEpubMapper> designToEpubMapperList) {
        Multimap<String, String> multiMap = ArrayListMultimap.create();
        for (DesignToEpubMapper designToEpubMapper : designToEpubMapperList) {
            multiMap.put(designToEpubMapper.getDesignName(), designToEpubMapper.getEpubName());
        }

        this.publicationEpubMapping = multiMap.asMap();
    }

    public Map<String, Collection<String>> getAllEpubMapping() {
        return allEpubMapping;
    }

    private void setAllEpubMapping(List<DesignToEpubMapper> mappingList) {
        Multimap<String, String> multiMap = ArrayListMultimap.create();
        for (DesignToEpubMapper designToEpubMapper : mappingList) {
            multiMap.put(designToEpubMapper.getDesignName(), designToEpubMapper.getEpubName());
        }

        this.allEpubMapping = multiMap.asMap();
    }

    public List<String> getUniqueDesignNameList() {
        return uniqueDesignNameList;
    }

    private void setUniqueDesignNameList(List<String> uniqueDesignNameList) {
        this.uniqueDesignNameList = uniqueDesignNameList;
    }
}
