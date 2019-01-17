package com.cefalo.cci.dao;

import com.cefalo.cci.model.Events;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface EventsDao {
    List<Events> getEventsByIssueId(long issueId, long start, long maxResult, String sortOrder, Date fromDate);

    long getEventsCountByIssueId(long issueId, Date fromDate);

    void saveEvents(Set<Events> eventSet);
}
