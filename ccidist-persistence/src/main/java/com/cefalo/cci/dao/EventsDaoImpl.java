package com.cefalo.cci.dao;

import com.cefalo.cci.model.Events;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class EventsDaoImpl extends EntityManagerDao implements EventsDao{

    @Override
    @SuppressWarnings("unchecked")
    public List<Events> getEventsByIssueId(long issueId, long start, long maxResult, String sortOrder, Date fromDate) {
        return getEntityManager().createQuery("select e from Events e where e.issueId =:Id and e.created >=:fromDate order by e.category " + sortOrder)
                .setHint("org.hibernate.cacheable", true)
                .setParameter("Id", issueId)
                .setParameter("fromDate", fromDate)
                .setFirstResult((int) start)
                .setMaxResults((int) maxResult)
                .getResultList();
    }

    @Override
    public long getEventsCountByIssueId(long issueId, Date fromDate) {
        return (Long) getEntityManager()
                .createQuery("select count(e) from Events e where e.issueId =:Id and e.created >=:fromDate")
                .setHint("org.hibernate.cacheable", true)
                .setParameter("Id", issueId)
                .setParameter("fromDate", fromDate)
                .getSingleResult();
    }

    @Override
    public void saveEvents(Set<Events> eventSet) {
        for (Events event : eventSet) {
            getEntityManager().persist(event);
        }
    }
}
