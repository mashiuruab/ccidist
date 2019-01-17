package com.cefalo.cci.action;


import static com.cefalo.cci.utils.StringUtils.isBlank;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.model.MatchingRules;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.service.MatchingService;
import com.cefalo.cci.service.PublicationService;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MatchingRuleListAction extends AuthenticatedActionSupport {
    /**
     * 
     */
    private static final long serialVersionUID = -385409622875728468L;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String publicationId;
    private List<MatchingRules> rulesList;
    private Publication publication;
    private PublicationService publicationService;
    private MatchingService matchingService;

    @Inject
    public MatchingRuleListAction(ApplicationConfiguration config, PublicationService publicationService, MatchingService matchingService) {
        super(config);
        this.publicationService = publicationService;
        this.matchingService = matchingService;
    }

    public String getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(String publicationId) {
        this.publicationId = publicationId;
    }

    public List<MatchingRules> getRulesList() {
        return rulesList;
    }

    public void setRulesList(List<MatchingRules> rulesList) {
        this.rulesList = rulesList;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public void validate() {
        super.validate();
        if (isBlank(getPublicationId())) {
            addActionError(getText("message.publication.not.found"));
        }
        if (!isBlank(getPublicationId()) && !isHasAccess()) {
            logger.error(String.format(getText("unauthorized.access")));
            addActionError(getText("unauthorized.access"));
        }
    }

    public String loadMatchingList() {
        setPagetTitle(getText("message.matchingList.title", new String[] {getPublication().getName()}));
        setRulesList(matchingService.getRulesByPublication(getPublicationId()));
        return "listLoaded";
    }

    private boolean isHasAccess() {
        setPublication(publicationService.getPublication(getPublicationId()));
        return isSuperUser()
                || (getPublication() != null && getPublication().getOrganization().getId().equals(getLoggedInUser().getUserPrivilege().getOrganization().getId()));
    }
}
