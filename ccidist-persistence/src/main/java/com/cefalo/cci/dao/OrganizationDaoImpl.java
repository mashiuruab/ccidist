package com.cefalo.cci.dao;

import java.util.List;

import com.cefalo.cci.model.Organization;
import com.google.inject.persist.Transactional;

public class OrganizationDaoImpl extends EntityManagerDao implements OrganizationDao {

    @SuppressWarnings("unchecked")
    @Override
    public List<Organization> getAllOrganizations() {
        return getEntityManager()
                .createQuery("select o from Organization o order by o.name asc")
                .setHint("org.hibernate.cacheable", true)
                .getResultList();
    }

    @Override
    public Organization getOrganization(String id) {
        return getEntityManager().find(Organization.class, id);
    }

    @Override
    @Transactional
    public void saveOrUpdate(Organization organization) {
        getEntityManager().persist(organization);
    }

    @Override
    @Transactional
    public void delete(String organizationId) {
        Organization organization = getOrganization(organizationId);
        getEntityManager().remove(organization);
    }
}
