package com.cefalo.cci.dao;

import java.util.List;

import com.cefalo.cci.model.MatchingRules;
import com.google.inject.persist.Transactional;

public class MatchingRulesDaoImpl extends EntityManagerDao implements MatchingRulesDao {
    @Override
    @Transactional
    public void saveMatchingRules(MatchingRules matchingRules) {
        getEntityManager().persist(matchingRules);
    }

    @Override
    public MatchingRules getMatchingRules(long ruleId) {
        return getEntityManager().find(MatchingRules.class, ruleId);
    }

    @Override
    @Transactional
    public void deleteMatchingRules(MatchingRules rule) {
        getEntityManager().remove(rule);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<MatchingRules> getRulesByPublicationId(String publicationId) {
        return getEntityManager()
                .createQuery(
                        "select mr From MatchingRules mr where "
                        + "mr.publication.id like :publicationId")
                .setParameter("publicationId", publicationId)
                .setHint("org.hibernate.cacheable", true)
                .getResultList();
    }
}
