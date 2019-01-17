package com.cefalo.cci.action.admin;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.model.Organization;
import com.cefalo.cci.service.OrganizationService;
import com.cefalo.cci.utils.StringUtils;

import javax.inject.Inject;

public class OrganizationDeleteAction extends AdministrativeActionSupport {
    private static final long serialVersionUID = 8936333384598024304L;

    private OrganizationService organizationService;

    private String organizationId;

    @Inject
    public OrganizationDeleteAction(ApplicationConfiguration config, OrganizationService organizationService) {
        super(config);

        this.organizationService = organizationService;
    }

    @Override
    public void validate() {
        super.validate();

        if (StringUtils.isBlank(organizationId)) {
            addFieldError("organizationId", getText("message.required"));
        } else {
            Organization organization = organizationService.getOrganization(organizationId);
            if (organization == null) {
                addFieldError("organizationId", getText("message.organization.id.invalid"));
            }
        }
    }

    public String deleteOrganization() {
        organizationService.delete(organizationId);

        return "deleted";
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }
}
