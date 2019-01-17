package com.cefalo.cci.action;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.model.Organization;
import com.cefalo.cci.service.OrganizationService;
import com.google.common.collect.Lists;

import javax.inject.Inject;
import java.util.List;

public class OrganizationListAction extends AuthenticatedActionSupport {
    private static final long serialVersionUID = -734629522295166175L;

    private OrganizationService organizationService;

    private List<Organization> organizationsBasedOnUser;
    private String organizationId;

    @Inject
    public OrganizationListAction(ApplicationConfiguration config, OrganizationService organizationService) {
        super(config);

        this.organizationService = organizationService;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public List<Organization> getAllOrganizations() {
        return organizationService.getAllOrganizations();
    }

    @Override
    public String execute() throws Exception {
        if (isSuperUser()) {
            setOrganizationsBasedOnUser(organizationService.getAllOrganizations());
        } else {
            String id = getLoggedInUser().getUserPrivilege().getOrganization().getId();
            Organization organization = organizationService.getOrganization(id);
            setOrganizationsBasedOnUser(Lists.newArrayList(organization));
        }

        setPagetTitle(getText("message.orgList.title"));
        return SUCCESS;
    }

    public void setOrganizationsBasedOnUser(List<Organization> organizationsBasedOnUser) {
        this.organizationsBasedOnUser = organizationsBasedOnUser;
    }

    public List<Organization> getOrganizationsBasedOnUser() {
        return organizationsBasedOnUser;
    }
}
