package com.cefalo.cci.action.admin;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.model.Organization;
import com.cefalo.cci.service.OrganizationService;

import javax.inject.Inject;

import static com.cefalo.cci.utils.StringUtils.isBlank;

public class OrganizationAction extends AdministrativeActionSupport {

    private static final long serialVersionUID = -592209874009097692L;

    private OrganizationService organizationService;

    private String organizationId;
    private String organizationName;

    /**
     * This is actually used as a flag to detect create/edit.
     */
    private long createdDate = -1;

    @Inject
    public OrganizationAction(ApplicationConfiguration config, OrganizationService organizationService) {
        super(config);

        this.organizationService = organizationService;
    }

    @Override
    public void validate() {
        super.validate();

        if ("GET".equalsIgnoreCase(getRequestMethod())) {
            if (!isBlank(organizationId) && organizationService.getOrganization(organizationId) == null) {
                addFieldError("organizationId", getText("message.organization.not.found"));
                return;
            }

            setPagetTitle(getText(isBlank(organizationId) ? "message.create.organization"
                    : "message.edit.organization"));
            return;
        }

        // All is POST after this line.
        if (isBlank(organizationId)) {
            addFieldError("organizationId", getText("message.required"));

        } else if (!organizationId.matches("^([A-Za-z]|[0-9]|-|_)+$")) {
            addFieldError("organizationId", getText("message.name.alphanumeric", new String[] {"Organization Id"}));
        }

        if (isBlank(organizationName)) {
            addFieldError("organizationName", getText("message.required"));
        }

        Organization organization = organizationService.getOrganization(organizationId);
        if (createdDate <= 0 && organization != null) {
            // Trying to create a new organization with an existing orgId
            addFieldError("organizationId", getText("message.id.used", new String[] { organizationId }));
        }

        if (createdDate > 0 && createdDate != organization.getCreated().getTime()) {
            addFieldError("organizationId", getText("message.id.used", new String[] { organizationId }));
        }

        setPagetTitle(getText(createdDate <= 0 ? "message.create.organization" : "message.edit.organization"));
    }

    public String populateOrganization() {
        if (!isBlank(organizationId)) {
            Organization organization = organizationService.getOrganization(organizationId);

            organizationId = organization.getId();
            organizationName = organization.getName();
            createdDate = organization.getCreated().getTime();

            setPagetTitle(getText("message.edit.organization"));
        } else {
            createdDate = -1;

            setPagetTitle(getText("message.create.organization"));
        }

        return "done";
    }

    public String saveOrUpdate() {
        Organization organization = organizationService.getOrganization(getOrganizationId());

        if (organization != null) {
            organization.setName(getOrganizationName());
            organizationService.updateOrganization(organization);
        } else {
            organization = new Organization();
            organization.setId(getOrganizationId());
            organization.setName(getOrganizationName());
            organizationService.createOrganization(organization);
        }

        return "redirect";
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }
}
