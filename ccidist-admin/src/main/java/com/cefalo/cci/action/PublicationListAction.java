package com.cefalo.cci.action;

import static com.cefalo.cci.utils.StringUtils.isBlank;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.model.Organization;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.service.OrganizationService;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PublicationListAction extends AuthenticatedActionSupport{
    /**
     * 
     */
    private static final long serialVersionUID = 1599326420394274920L;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String organizationId;
    private OrganizationService organizationService;
    private Organization organization;
    private List<Publication> publications;

    @Inject
    public PublicationListAction(ApplicationConfiguration config, OrganizationService organizationService) {
        super(config);
        this.organizationService = organizationService;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public List<Publication> getPublications() {
        return publications;
    }

    public void setPublications(List<Publication> publications) {
        this.publications = publications;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public void validate() {
        super.validate();
        if (isBlank(getOrganizationId())) {
            addFieldError("organizationId", getText("message.required"));
        }
        if (!isSuperUser()
                && !getOrganizationId().equals(getLoggedInUser().getUserPrivilege().getOrganization().getId())) {
            logger.error(String.format(getText("message.organization.invalid")));
            addActionError(getText("unauthorized.access"));
        }
    }

    public String loadPublicationList() {
        setPublications(getPublicationsInOrganization());
        setPagetTitle(getText("message.publicationList.title", new String[] {getOrganization() != null ? getOrganization().getName() : ""}));
        return "done";
    }

    private List<Publication> getPublicationsInOrganization() {
        setOrganization(organizationService.getOrganization(getOrganizationId()));
        if (getOrganization() != null) {
            return getOrganization().getPublications();
        }
        addActionError(getText("unauthorized.access"));
        return null;
    }
}
