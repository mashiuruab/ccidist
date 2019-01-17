package com.cefalo.cci.service;

import com.cefalo.cci.dao.EventsDao;
import com.cefalo.cci.enums.Category;
import com.cefalo.cci.utils.locator.ResourceLocator;
import com.cefalo.cci.model.Events;
import com.cefalo.cci.model.Issue;
import com.cefalo.cci.model.Organization;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.utils.AtomUtils;
import com.google.inject.Inject;
import com.sun.syndication.feed.synd.*;
import org.joda.time.format.DateTimeFormat;

import java.util.*;

public class ChangelogServiceImpl implements ChangelogService {
    @Inject
    private EventsDao eventsDao;

    @Override
    public void addChanges(long issueId, Set<String> updatedSet, Set<String> insertedSet, Set<String> deletedSet) {
        if (updatedSet.isEmpty() && insertedSet.isEmpty() && deletedSet.isEmpty()) {
            return;
        }

        Set<Events> events = new HashSet<>();
        processFileSetAndAddEvents(issueId, Category.UPDATED.getValue(), updatedSet, events);
        processFileSetAndAddEvents(issueId, Category.INSERTED.getValue(), insertedSet, events);
        processFileSetAndAddEvents(issueId, Category.DELETED.getValue(), deletedSet, events);

        eventsDao.saveEvents(events);
    }

    @Override
    public SyndFeed getChangelogAtomFeed(
            Issue issue,
            long start, long limit,
            String epubName,
            Date fromDate,
            String sortOrder,
            ResourceLocator resourceLocator) {
        // Remember that the DB layer expects 0 based indexing while we use 1 based indexing in the resource layer.
        return generateEventQueueAtomFeed(
                eventsDao.getEventsByIssueId(issue.getId(), start - 1, limit, sortOrder, fromDate),
                issue,
                start,
                limit,
                eventsDao.getEventsCountByIssueId(issue.getId(), fromDate),
                epubName,
                fromDate,
                sortOrder,
                resourceLocator);
    }

    @SuppressWarnings("unchecked")
    private SyndFeed generateEventQueueAtomFeed(
            List<Events> eventsList,
            Issue issue,
            long start, long limit,
            long total,
            String epubName,
            Date fromDate,
            String sortOrder,
            ResourceLocator resourceLocator) {
        Organization organization = issue.getPublication().getOrganization();
        Publication publication = issue.getPublication();
        String publicationName = publication.getName();

        DateTimeFormat.forPattern("yyyy-MM-dd");
        Map<String, String> queryParams = new LinkedHashMap<>();

        SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType("atom_1.0");
        feed.setTitle(String.format("%s %s Updates", publicationName, issue.getName()));
        feed.setPublishedDate(issue.getUpdated());

        SyndPerson syndPerson = new SyndPersonImpl();
        syndPerson.setName(publicationName);
        feed.getAuthors().add(syndPerson);
        feed.setUri("urn:uuid:".concat(String.valueOf(issue.getId())));

        List<SyndLink> links = AtomUtils.getLinks(
                start,
                limit,
                total,
                resourceLocator.getEventQueueURI(
                        organization.getId(),
                        publication.getId(),
                        issue.getId()).toString(),
                queryParams);
        feed.setLinks(links);

        List<SyndEntry> entries = new ArrayList<SyndEntry>();
        SyndCategory syndCategory;

        for (Events events : eventsList) {
            SyndEntryImpl entry = new SyndEntryImpl();
            entry.setUri("urn:".concat(String.valueOf(events.getId())));
            entry.setTitle(events.getPath());
            entry.setAuthor(publicationName);
            entry.setLink(resourceLocator.getEpubContentURI(
                    organization.getId(),
                    publication.getId(),
                    issue.getId(),
                    events.getPath()).toString());
            entry.setUpdatedDate(events.getCreated());

            syndCategory = new SyndCategoryImpl();
            syndCategory.setName(getCategory(events.getCategory()));
            entry.getCategories().add(syndCategory);
            entries.add(entry);
        }

        feed.setEntries(entries);
        return feed;
    }

    private String getCategory(int value) {
        if (Category.INSERTED.getValue() == value) {
            return Category.INSERTED.toString();
        } else if (Category.UPDATED.getValue() == value) {
            return Category.UPDATED.toString();
        } else if (Category.DELETED.getValue() == value) {
            return Category.DELETED.toString();
        }
        return null;
    }

    private void processFileSetAndAddEvents(long issueId, int fileStatus, Set<String> fileSet, Set<Events> events) {
        if (!fileSet.isEmpty()) {
            for (String filePath : fileSet) {
                Events event = new Events();
                event.setIssueId(issueId);
                event.setCategory(fileStatus);
                event.setPath(filePath);
                events.add(event);
            }
        }
    }
}
