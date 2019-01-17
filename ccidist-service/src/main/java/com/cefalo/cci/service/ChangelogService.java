package com.cefalo.cci.service;

import com.cefalo.cci.utils.locator.ResourceLocator;
import com.cefalo.cci.model.Issue;
import com.sun.syndication.feed.synd.SyndFeed;

import java.util.Date;
import java.util.Set;

public interface ChangelogService {
    void addChanges(
            long issueId,
            Set<String> updatedSet,
            Set<String> insertedSet,
            Set<String> deletedSet);

    SyndFeed getChangelogAtomFeed(
            Issue issue,
            long start, long limit,
            String epubName,
            Date fromDate,
            String sortOrder,
            ResourceLocator resourceLocator);
}
