package com.cefalo.cci.service;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import com.cefalo.cci.dao.PublicationDao;
import com.cefalo.cci.event.manager.EventManager;
import com.cefalo.cci.event.model.Event;
import com.cefalo.cci.event.model.EventType;
import com.cefalo.cci.utils.locator.ResourceLocator;
import com.cefalo.cci.model.Issue;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.model.RxmlZipFile;
import com.google.inject.persist.Transactional;

public class PublicationServiceImpl implements PublicationService {

    @Inject
    private PublicationDao publicationDao;

    @Inject
    private IssueService issueService;

    @Inject
    private RxmlService rxmlService;

    @Inject
    private EventManager eventManager;

    @Inject
    @Named("webservice")
    private Provider<ResourceLocator> webserviceLocatorProvider;

    @Override
    public Publication getPublication(String publicationId) {
        return publicationDao.getPublication(publicationId);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void delete(Publication publication) {
        String publicationId = publication.getId();
        String organizationId = publication.getOrganization().getId();

        // First delete all the issues.
        List<Issue> issueList = issueService.getIssuesOfPublication(publication);
        for (Issue issue : issueList) {
            issueService.deleteIssue(issue);
        }

        // Now cleanup the RXML binary storage
        List<RxmlZipFile> allRxmlFiles = rxmlService.getAllRxmlFiles(publication);
        for (RxmlZipFile rxmlFile : allRxmlFiles) {
            rxmlService.delete(rxmlFile);
        }

        publicationDao.delete(publication);
        eventManager.post(new Event(EventType.DELETE, Publication.class, publicationId, organizationId));
    }

    @Override
    public void createPublication(Publication publication) {
        publicationDao.saveOrUpdate(publication);

        eventManager.post(new Event(EventType.CREATE, Publication.class, publication.getId(), null));
    }

    @Override
    public void updatePublication(Publication publication) {
        publicationDao.saveOrUpdate(publication);

        eventManager.post(new Event(EventType.UPDATE, Publication.class, publication.getId(), null));
    }
}
