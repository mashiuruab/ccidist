package com.cefalo.cci.dao;

import com.cefalo.cci.model.Organization;

import java.util.List;

public interface OrganizationDao {
    List<Organization> getAllOrganizations();

    Organization getOrganization(String id);

    void saveOrUpdate(Organization organization);

    void delete(String organizationId);
}
