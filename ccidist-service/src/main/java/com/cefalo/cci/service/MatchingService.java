package com.cefalo.cci.service;

import com.cefalo.cci.model.Content;
import com.cefalo.cci.model.DesignToEpubMapper;
import com.cefalo.cci.model.MatchingRules;
import com.cefalo.cci.model.Publication;

import java.util.List;

public interface MatchingService {
    List<MatchingRules> getRulesByPublication(String publicationId);

    MatchingRules getMatchingRules(long matchingId);

    void saveMathchingRulesAndDesignMapper(MatchingRules matchingRules, DesignToEpubMapper designToEpubMapper);

    void deleteMatchingRules(long ruleId);

    public List<MatchingRules> applyMatchingAlgorithm(Content content, Publication publication);
}
