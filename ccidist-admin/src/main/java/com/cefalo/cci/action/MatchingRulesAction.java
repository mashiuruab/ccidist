package com.cefalo.cci.action;

import static com.cefalo.cci.utils.StringUtils.isBlank;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.dao.DriverDao;
import com.cefalo.cci.model.DesignToEpubMapper;
import com.cefalo.cci.model.MatchingRules;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.service.MatchingService;
import com.cefalo.cci.service.PublicationService;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;

public class MatchingRulesAction extends AuthenticatedActionSupport {
    private static final long serialVersionUID = 2777949884363062558L;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private MatchingService matchingService;
    private DriverDao driverDao;
    private PublicationService publicationService;

    private String publicationId;
    private long ruleId = 0;
    private MatchingRules matchingRules;
    private Map<String, Collection<String>> designToEpubMapper;
    private int width;
    private int height;
    private String os;
    private String osv;
    private String readerVersion;
    private String deviceName;
    private String designName;
    private String epubName;

    private List<String> uniqueDesignNameList;

    @Inject
    public MatchingRulesAction(ApplicationConfiguration config, MatchingService matchingService, DriverDao driverDao,
            PublicationService publicationService) {
        super(config);

        this.matchingService = matchingService;
        this.driverDao = driverDao;
        this.publicationService = publicationService;
    }

    @Override
    public void validate() {
        super.validate();

        checkForValidOrganization();

        if ("GET".equals(getRequestMethod())) {
            validateGetRequest();
        }
        if ("POST".equals(getRequestMethod())) {
            validatePostRequest();
        }

        if (ruleId == 0) {
            setPagetTitle(getText("message.create.matching.rules"));
        } else {
            setPagetTitle(getText("message.edit.matching.rules"));
        }

        setDesignToEpubMapper(driverDao.getAllDesignToEpubMapperByPublicationId(getPublicationId()));
        List<DesignToEpubMapper> designToEpubMapperList = driverDao
                .getAllDesignToEpubMapperByPublicationId(getPublicationId());

        // Find the unique design names for this publication.
        Set<String> designNameSet = new HashSet<String>();
        for (DesignToEpubMapper designToEpubMapper : designToEpubMapperList) {
            designNameSet.add(designToEpubMapper.getDesignName());
        }
        setUniqueDesignNameList(Lists.newArrayList(designNameSet));
    }

    private void validateGetRequest() {
        if (isBlank(getPublicationId())) {
            addActionError(getText("unauthorized.access"));
        }

        if (width > 0 || height > 0 || !isBlank(getOs()) || !isBlank(getOsv())
                || !isBlank(getDeviceName()) || !isBlank(getDesignName()) || !isBlank(getEpubName())
                || !isBlank(getReaderVersion())) {
            addActionError(getText("unauthorized.access"));
        }
    }

    private void validatePostRequest() {
        if (isBlank(epubName)) {
            addFieldError("epubName", getText("message.required"));
        }

        if (isBlank(designName)) {
            addFieldError("designName", getText("message.required"));
        }

        if (!isBlank(designName) && !isBlank(epubName)) {
            DesignToEpubMapper designToEpubMapper =
                    driverDao.getDesignToEpubMapper(designName.toLowerCase(), epubName.toLowerCase());
            if (designToEpubMapper == null) {
                addActionError(getText("message.design.epub.combination"));
            }
        }
    }

    public String onLoad() {
        if (ruleId > 0) {
            matchingRules = matchingService.getMatchingRules(getRuleId());
            setWidth(matchingRules.getWidth());
            setHeight(matchingRules.getHeight());
            setOs(matchingRules.getOs());
            setOsv(matchingRules.getOsv());
            setReaderVersion(matchingRules.getReaderVersion());
            setDeviceName(matchingRules.getDeviceName());
            setDesignName(matchingRules.getDesignToEpubMapper().getDesignName());
            setEpubName(matchingRules.getDesignToEpubMapper().getEpubName());
            setPagetTitle(getText("message.edit.matching.rules"));
        } else {
            setPagetTitle(getText("message.create.matching.rules"));
        }
        setDesignToEpubMapper(driverDao.getAllDesignToEpubMapperByPublicationId(getPublicationId()));
        return "loaded";
    }

    public String saveOrUpdate() {
        if (ruleId == 0) { // new entity save
            matchingRules = new MatchingRules();
        } else {
            matchingRules = matchingService.getMatchingRules(ruleId);
        }
        matchingRules.setPublication(publicationService.getPublication(getPublicationId()));
        matchingRules.setWidth(getWidth());
        matchingRules.setHeight(getHeight());
        matchingRules.setOs(getOs());
        matchingRules.setOsv(getOsv());
        matchingRules.setReaderVersion(getReaderVersion());
        matchingRules.setDeviceName(getDeviceName());
        matchingService.saveMathchingRulesAndDesignMapper(matchingRules,
                getDesignToEpubMapper(getDesignName().toLowerCase(), getEpubName().toLowerCase()));
        return "saved";
    }

    public DesignToEpubMapper getDesignToEpubMapper(String designName, String epubName) {
        return driverDao.getDesignToEpubMapper(designName.toLowerCase(), epubName.toLowerCase());
    }

    public void checkForValidOrganization() {
        if (!isHasAccess()) {
            logger.error(String.format(getText("message.organization.invalid")));
            addActionError("You do not have access to this publication");
        }
    }

    public boolean isHasAccess() {
        Publication publication = publicationService.getPublication(getPublicationId());
        return isSuperUser()
                || (publication != null && publication.getOrganization().getId()
                        .equals(getLoggedInUser().getUserPrivilege().getOrganization().getId()));
    }

    public String getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(String publicationId) {
        this.publicationId = publicationId;
    }

    public long getRuleId() {
        return ruleId;
    }

    public void setRuleId(long ruleId) {
        this.ruleId = ruleId;
    }

    public Map<String, Collection<String>> getDesignToEpubMapper() {
        return designToEpubMapper;
    }

    public void setDesignToEpubMapper(List<DesignToEpubMapper> designToEpubMapperList) {
        Multimap<String, String> multiMap = ArrayListMultimap.create();
        for (DesignToEpubMapper designToEpubMapper : designToEpubMapperList) {
            multiMap.put(designToEpubMapper.getDesignName(), designToEpubMapper.getEpubName());
        }

        this.designToEpubMapper = multiMap.asMap();
    }

    public List<String> getUniqueDesignNameList() {
        return uniqueDesignNameList;
    }

    public void setUniqueDesignNameList(List<String> uniqueDesignNameList) {
        this.uniqueDesignNameList = uniqueDesignNameList;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getOsv() {
        return osv;
    }

    public void setOsv(String osv) {
        this.osv = osv;
    }

    public String getReaderVersion() {
        return readerVersion;
    }

    public void setReaderVersion(String readerVersion) {
        this.readerVersion = readerVersion;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDesignName() {
        return designName;
    }

    public void setDesignName(String designName) {
        this.designName = designName;
    }

    public String getEpubName() {
        return epubName;
    }

    public void setEpubName(String epubName) {
        this.epubName = epubName;
    }
}
