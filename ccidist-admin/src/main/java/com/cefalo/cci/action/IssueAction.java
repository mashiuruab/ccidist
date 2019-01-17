package com.cefalo.cci.action;

import static com.cefalo.cci.utils.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.joda.time.DateTime;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.enums.IssueStatus;
import com.cefalo.cci.model.Issue;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.service.DriverInfoService;
import com.cefalo.cci.service.IssueService;
import com.cefalo.cci.service.PublicationService;
import com.cefalo.cci.utils.DateUtils;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

public class IssueAction extends AuthenticatedActionSupport {
    private static final long serialVersionUID = -5343869885175459228L;

    private static final String STRUTS_CHECKBOX_PREFIX = "__checkbox_";
    private static final String DELETE_FIELD_PREFIX = "delete_";
    private static final String STATUS_FIELD_PREFIX = "status_";

    private IssueService issueService;
    private PublicationService publicationService;
    private DriverInfoService driverInfoService;

    private String publicationId;
    private Publication publication;

    private int start;
    private int limit;
    private Date toDate;
    private String epubName;
    private int issueStatus;
    private String organizationName;
    private String publicationName;

    private List<Issue> issueList;
    private List<String> allEpubNames;

    @Inject
    public IssueAction(ApplicationConfiguration config, IssueService issueService,
            PublicationService publicationService, DriverInfoService driverInfoService) {
        super(config);

        this.issueService = issueService;
        this.publicationService = publicationService;
        this.driverInfoService = driverInfoService;
    }

    public String getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(String publicationId) {
        this.publicationId = publicationId;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    @TypeConversion(converter = "com.cefalo.cci.action.DateConverter")
    public Date getToDate() {
        return toDate;
    }

    @TypeConversion(converter = "com.cefalo.cci.action.DateConverter")
    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public String getEpubName() {
        return epubName;
    }

    public void setEpubName(String epubName) {
        this.epubName = epubName;
    }

    public int getIssueStatus() {
        return issueStatus;
    }

    public void setIssueStatus(int issueStatus) {
        this.issueStatus = issueStatus;
    }

    public List<IssueStatus> getIssueStatusList() {
        return Lists.newArrayList(IssueStatus.values());
    }

    public List<String> getAllEpubNames() {
        if (allEpubNames == null) {
            allEpubNames = driverInfoService.getUniqueEpubNames(getPublicationId());
        }

        return allEpubNames;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getPublicationName() {
        return publicationName;
    }

    public void setPublicationName(String publicationName) {
        this.publicationName = publicationName;
    }

    public List<Issue> getIssueList() {
        return issueList;
    }

    public void setIssueList(List<Issue> issueList) {
        this.issueList = issueList;
    }

    public String checkPrivilege() {
        setPublication(publicationService.getPublication(getPublicationId()));
        if (!isSuperUser()
                && !getPublication().getOrganization().getId()
                        .equals(getLoggedInUser().getUserPrivilege().getOrganization().getId())) {
            throw new RuntimeException("Unauhorized access");
        }
        if (!isBlank(publicationId)) {
            if (getPublication() != null) {
                setPublicationName(getPublication().getName());
                setOrganizationName(getPublication().getOrganization().getName());
                populateValues();

                List<Issue> issues = issueService.getAdminIssueListBySearchQuery(
                        publicationId,
                        start,
                        limit,
                        epubName,
                        (getIssueStatus() <= 0)
                                ? Lists.newArrayList(IssueStatus.values()) // Issues with any status
                                : Lists.newArrayList(IssueStatus.valueOf(issueStatus)), // Just the selected status
                        toDate);
                setIssueList(issues);
            }
            setPagetTitle(getText("message.issue.search", new String[] { getPublication().getOrganization().getName(),
                    getPublication().getName() }));
        } else {
            setIssueList(null);
            setPagetTitle(getText("message.issue.search", new String[] { "", "" }));
        }
        return "done";
    }

    private void populateValues() {
        DateTime date = new DateTime();
        date = date.plusDays(1);

        if (start <= 0) {
            start = 1;
        }

        if (limit <= 0) {
            limit = 10;
        }

        toDate = getToDate() == null ? DateUtils.convertDateWithTZ(date.toDate()) : DateUtils.convertDateWithTZ(getToDate());
        epubName = Strings.nullToEmpty(epubName);
    }

    public String updateIssues() {
        List<Long> deletableIssueIds = getIssuesForDelete();

        Map<Long, Boolean> issueStatusValues = getBooleanValues(STATUS_FIELD_PREFIX);
        Map<Long, IssueStatus> issueIdStatusMap = new HashMap<>();
        for (Entry<Long, Boolean> entry : issueStatusValues.entrySet()) {
            issueIdStatusMap.put(entry.getKey(), entry.getValue() ? IssueStatus.PUBLISHED : IssueStatus.DRAFT);
        }

        issueService.updateStatusAndDeleteIssue(issueIdStatusMap, deletableIssueIds);
        return "redirect";
    }

    private List<Long> getIssuesForDelete() {
        Map<Long, Boolean> issueValues = getBooleanValues(DELETE_FIELD_PREFIX);
        List<Long> issuesToDelete = new ArrayList<>();
        for (Entry<Long, Boolean> entry : issueValues.entrySet()) {
            if (entry.getValue()) {
                issuesToDelete.add(entry.getKey());
            }
        }
        return issuesToDelete;
    }

    private Map<Long, Boolean> getBooleanValues(String fieldPrefix) {
        Map<Long, Boolean> fieldValues = new HashMap<>();
        String fieldName = STRUTS_CHECKBOX_PREFIX.concat(fieldPrefix);

        Map<String, String[]> requestMap = httpRequest.getParameterMap();
        for (Map.Entry<String, String[]> entry : requestMap.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(fieldName)) {
                Long issueId = Long.parseLong(key.substring(fieldName.length()));
                String[] fieldValueArray = requestMap.get(fieldPrefix + issueId);
                Boolean value = (fieldValueArray == null || fieldValueArray.length == 0) ? Boolean.FALSE : Boolean
                        .parseBoolean(fieldValueArray[0]);
                fieldValues.put(issueId, value);
            }
        }

        return fieldValues;
    }
}
