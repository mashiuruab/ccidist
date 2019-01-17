package com.cefalo.cci.action;

import static com.cefalo.cci.utils.StringUtils.isBlank;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.service.PublicationService;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublicationDeleteAction extends AuthenticatedActionSupport {
    private static final long serialVersionUID = -6138678529629937425L;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String organizationId;
    private String publicationId;
    private PublicationService publicationService;

    @Inject
    public PublicationDeleteAction(ApplicationConfiguration config, PublicationService publicationService) {
        super(config);

        this.publicationService = publicationService;
    }

    @Override
    public void validate() {
        super.validate();
        if (isBlank(getOrganizationId())) {
            String errorMsg = getText("message.organization.not.found");
            logger.error(errorMsg);
            addActionError(errorMsg);
        }
        if (isBlank(getPublicationId())) {
            String errorMsg = getText("message.publication.not.found");
            logger.error(errorMsg);
            addActionError(errorMsg);
        }
        if (!isSuperUser()
                && !getOrganizationId().equals(getLoggedInUser().getUserPrivilege().getOrganization().getId())) {
            logger.error(getText("message.organization.invalid"));
            addActionError(getText("unauthorized.access"));
        }
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(String publicationId) {
        this.publicationId = publicationId;
    }

    public String deletePublication() {
        Publication publication = publicationService.getPublication(publicationId);
        publicationService.delete(publication);

        return "deleted";
    }
}
