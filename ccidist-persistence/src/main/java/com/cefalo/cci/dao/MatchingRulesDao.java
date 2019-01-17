package com.cefalo.cci.dao;

import com.cefalo.cci.model.MatchingRules;

import java.util.List;

public interface MatchingRulesDao {
    void saveMatchingRules(MatchingRules matchingRules);

    MatchingRules getMatchingRules(long ruleId);

    void deleteMatchingRules(MatchingRules rule);

    List<MatchingRules> getRulesByPublicationId(String publicationId);
}
