package com.cefalo.cci.action;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.model.Organization;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.service.OrganizationService;
import com.cefalo.cci.service.PublicationService;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

import static com.cefalo.cci.utils.StringUtils.isBlank;

public class PublicationAction extends AuthenticatedActionSupport {
    private static final long serialVersionUID = 3171952182399831202L;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private PublicationService publicationService;
    private OrganizationService organizationService;

    private String publicationId;
    private String organizationId;
    private String publicationName;
    private String publicationCreateDate;

    private List<Publication> publications;

    /**
     * This is actually used as a flag to detect create/edit.
     */
    private long createdDate = -1;

    @Inject
    public PublicationAction(ApplicationConfiguration config, PublicationService publicationService, OrganizationService organizationService) {
        super(config);

        this.publicationService = publicationService;
        this.organizationService = organizationService;
    }

    public List<Publication> getPublications() {
        return publications;
    }

    public void setPublications(List<Publication> publications) {
        this.publications = publications;
    }

    public String privilegeChecking() {
        checkForValidOrganization();
        String publicationId = getPublicationId();
        if (Strings.isNullOrEmpty(publicationId)) {
            String organizationId = getOrganizationId();
            if (Strings.isNullOrEmpty(organizationId)) {
                logger.error(String.format(getText("message.organization.not.found")));
                throw new RuntimeException(getText("message.organization.not.found"));
            }
        }
        loadPublication();
        return "done";
    }

    public void loadPublication() {
        String publicationId = getPublicationId();
        if (isBlank(publicationId)) {
            setOrganizationId(organizationId);
            setPublicationCreateDate("");
            setPagetTitle(getText("message.create.publication"));
        } else {
            Publication publication = publicationService.getPublication(publicationId);
            setOrganizationId(publication.getOrganization().getId());
            setPublicationId(publication.getId());
            setPublicationName(publication.getName());
            setPublicationCreateDate(String.valueOf(publication.getCreated().getTime()));
            setPagetTitle(getText("message.edit.publication"));
            setCreatedDate(publication.getCreated().getTime());
        }
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

    public String getPublicationName() {
        return publicationName;
    }

    public void setPublicationName(String publicationName) {
        this.publicationName = publicationName;
    }

    public String getPublicationCreateDate() {
        return publicationCreateDate;
    }

    public void setPublicationCreateDate(String publicationCreateDate) {
        this.publicationCreateDate = publicationCreateDate;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String execute() throws Exception {
        return SUCCESS;
    }

    @Override
    public void validate() {
        super.validate();

        if ("GET".equals(getRequestMethod())) {
            validateGetRequests();
        }

        if ("POST".equals(getRequestMethod())) {
            validatePostRequests();
        }

        /*This is for setting the title message after validation*/
        if (isBlank(getPublicationId())) {
            setPagetTitle(getText("message.create.publication"));
        } else {
            setPagetTitle(getText("message.edit.publication"));
        }
    }

    private void validateGetRequests() {
        if (!isBlank(getPublicationName())) {
            logger.error(getText("unauthorized.access"));
            addActionError(getText("unauthorized.access"));
        }
        if (isBlank(getOrganizationId())) {
            addFieldError("organizationId", getText("message.orgId.required"));
        }
    }

    private void validatePostRequests() {
        if (isBlank(getPublicationId())) {
            addFieldError("publicationId", getText("message.publication.id.required"));
        } else if (!getPublicationId().matches("^([A-Za-z]|[0-9]|-|_)+$")) {
            addFieldError("publicationId", getText("message.name.alphanumeric", new String[] {"Publication Id"}));
        }

        if (isBlank(getPublicationName())) {
            addFieldError("publicationName", getText("message.publication.name.required"));
        }

        if (isBlank(getOrganizationId())) {
            addFieldError("organizationId", getText("message.orgId.required"));
        }

        if (getPublicationCreateDate() != null && !isBlank(getPublicationId())) {
            Publication publication = publicationService.getPublication(getPublicationId());
            if (publication != null && !String.valueOf(publication.getCreated().getTime()).equals(getPublicationCreateDate())) {
                addActionError(getText("message.id.used", new String[] {getPublicationId()}));
            }
        }
    }

    public String saveOrUpdate() {
        Publication publication = publicationService.getPublication(getPublicationId());
        if (publication != null) {
            publication.setName(getPublicationName());
            publicationService.updatePublication(publication);
        } else {
            Organization organization = organizationService.getOrganization(getOrganizationId());
            publication = new Publication();
            publication.setId(getPublicationId());
            publication.setName(getPublicationName());
            publication.setOrganization(organization);
            publicationService.createPublication(publication);
        }
        return "redirect";
    }

    public void checkForValidOrganization() {
        if (!isSuperUser()
                && !getOrganizationId().equals(getLoggedInUser().getUserPrivilege().getOrganization().getId())) {
            logger.error(String.format(getText("message.organization.invalid")));
            throw new RuntimeException("Unauhorized access");
        }
    }
}
