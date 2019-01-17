package com.cefalo.cci.action;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.service.DriverInfoService;
import com.cefalo.cci.service.PublicationService;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.cefalo.cci.utils.StringUtils.isBlank;

public class DriverRuleDeleteAction extends AuthenticatedActionSupport {
    /**
     *
     */
    private static final long serialVersionUID = -6494037772793328084L;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String driverInfoId;
    private String publicationId;

    private PublicationService publicationService;
    private DriverInfoService driverInfoService;

    @Inject
    public DriverRuleDeleteAction(ApplicationConfiguration config, PublicationService publicationService, DriverInfoService driverInfoService) {
        super(config);
        this.publicationService = publicationService;
        this.driverInfoService = driverInfoService;
    }

    public String getDriverInfoId() {
        return driverInfoId;
    }

    public void setDriverInfoId(String driverInfoId) {
        this.driverInfoId = driverInfoId;
    }

    public String getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(String publicationId) {
        this.publicationId = publicationId;
    }

    @Override
    public void validate() {
        super.validate();
        if (isBlank(getPublicationId())) {
            addActionError(getText("message.publication.not.found"));
        }
        if (isBlank(getDriverInfoId())) {
            addActionError(getText("message.driverId.not.found"));
        }
        if (!isBlank(getPublicationId()) && !isHasAccess()) {
            logger.error(String.format(getText("unauthorized.access")));
            addActionError(getText("unauthorized.access"));
        }
    }

    public String deleteDriver() {
        driverInfoService.deleteDriver(Long.valueOf(getDriverInfoId()));
        return "deleted";
    }

    private boolean isHasAccess() {
        Publication publication = publicationService.getPublication(getPublicationId());
        return isSuperUser()
                || (publication != null && publication.getOrganization().getId().equals(getLoggedInUser().getUserPrivilege().getOrganization().getId()));
    }
}
