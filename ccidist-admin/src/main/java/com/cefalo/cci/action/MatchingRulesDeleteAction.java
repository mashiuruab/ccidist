package com.cefalo.cci.action;

import static com.cefalo.cci.utils.StringUtils.isBlank;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.service.MatchingService;
import com.cefalo.cci.service.PublicationService;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatchingRulesDeleteAction extends AuthenticatedActionSupport{
    /**
     * 
     */
    private static final long serialVersionUID = -1457921659798740307L;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String publicationId;
    private String ruleId;

    private PublicationService publicationService;
    private MatchingService matchingService;

    @Inject
    public MatchingRulesDeleteAction(ApplicationConfiguration config, PublicationService publicationService, MatchingService matchingService) {
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

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public void validate() {
        super.validate();
        if (isBlank(getPublicationId())) {
            addActionError(getText("message.publication.not.found"));
        }
        if (isBlank(getRuleId())) {
            addActionError(getText("message.matchingRuleId.not.found"));
        }
        if (!isBlank(getPublicationId()) && !isHasAccess()) {
            logger.error(String.format(getText("unauthorized.access")));
            addActionError(getText("unauthorized.access"));
        }
    }

    public String deleteRules() {
        matchingService.deleteMatchingRules(Long.valueOf(getRuleId()));
        return "deleted";
    }

    private boolean isHasAccess() {
        Publication publication = publicationService.getPublication(getPublicationId());
        return isSuperUser()
                || (publication != null && publication.getOrganization().getId().equals(getLoggedInUser().getUserPrivilege().getOrganization().getId()));
    }
}
