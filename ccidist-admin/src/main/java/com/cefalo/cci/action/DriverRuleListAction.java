package com.cefalo.cci.action;

import static com.cefalo.cci.utils.StringUtils.isBlank;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.model.DriverInfo;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.service.DriverInfoService;
import com.cefalo.cci.service.PublicationService;
import com.google.inject.Inject;

public class DriverRuleListAction extends AuthenticatedActionSupport{
    private static final long serialVersionUID = -2393940902193946014L;

    private final Logger logger = LoggerFactory.getLogger(DriverRuleListAction.class);

    private String publicationId;
    private List<DriverInfo> driverInfoList;
    private Publication publication;

    private PublicationService publicationService;
    private DriverInfoService driverInfoService;

    @Inject
    public DriverRuleListAction(ApplicationConfiguration config, PublicationService publicationService, DriverInfoService driverInfoService) {
        super(config);
        this.publicationService = publicationService;
        this.driverInfoService = driverInfoService;
    }

    public String getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(String publicationId) {
        this.publicationId = publicationId;
    }

    public List<DriverInfo> getDriverInfoList() {
        return driverInfoList;
    }

    public void setDriverInfoList(List<DriverInfo> driverInfoList) {
        this.driverInfoList = driverInfoList;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    @Override
    public void validate() {
        super.validate();

        if (isBlank(getPublicationId())) {
            logger.error(String.format(getText("message.publication.not.found")));
            addActionError(getText("message.publication.not.found"));
        }
        if (!isBlank(getPublicationId()) && !isHasAccess()) {
            logger.error(String.format(getText("unauthorized.access")));
            addActionError(getText("unauthorized.access"));
        }
    }

    public String loadDriverInfoList() {
        setDriverInfoList(driverInfoService.getDrivers(getPublicationId()));

        setPagetTitle(getText("message.driverList.title", new String[] {getPublication().getName()}));
        return "done";
    }

    private boolean isHasAccess() {
        setPublication(publicationService.getPublication(getPublicationId()));
        return isSuperUser()
                || (getPublication() != null && getPublication().getOrganization().getId()
                        .equals(getLoggedInUser().getUserPrivilege().getOrganization().getId()));
    }
}
