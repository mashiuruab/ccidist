package com.cefalo.cci.service;

import com.cefalo.cci.dao.OrganizationDao;
import com.cefalo.cci.event.manager.EventManager;
import com.cefalo.cci.event.model.*;
import com.cefalo.cci.utils.locator.ResourceLocator;
import com.cefalo.cci.model.Organization;
import com.cefalo.cci.model.Publication;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

import javax.inject.Named;

import java.util.List;

public class OrganizationServiceImpl implements OrganizationService {
    @Inject
    private OrganizationDao organizationDao;

    @Inject
    private PublicationService publicationService;

    @Inject
    private EventManager eventManager;

    @Inject
    @Named("webservice")
    private Provider<ResourceLocator> webserviceLocatorProvider;

    @Override
    public List<Organization> getAllOrganizations() {
        return organizationDao.getAllOrganizations();
    }

    @Override
    public Organization getOrganization(String id) {
        return organizationDao.getOrganization(id);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void delete(String organizationId) {
        Organization org = getOrganization(organizationId);
        for (Publication publication : org.getPublications()) {
            publicationService.delete(publication);
        }
        organizationDao.delete(organizationId);

        eventManager.post(new Event(EventType.DELETE, Organization.class, organizationId, null));
    }

    @Override
    @Transactional
    public void createOrganization(Organization organization) {
        organizationDao.saveOrUpdate(organization);

        eventManager.post(new Event(EventType.CREATE, Organization.class, organization.getId(), null));
    }

    @Override
    @Transactional
    public void updateOrganization(Organization organization) {
        organizationDao.saveOrUpdate(organization);

        eventManager.post(new Event(EventType.UPDATE, Organization.class, organization.getId(), null));
    }
}
