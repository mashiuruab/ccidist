package com.cefalo.cci.action.admin;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cefalo.cci.config.ApplicationConfiguration;

public class MonitorAction extends AdministrativeActionSupport {
    private static final long serialVersionUID = 7963330846288395890L;

    private final Logger logger = LoggerFactory.getLogger(MonitorAction.class);

    private boolean enabled;
    private String redirectURL;

    @Inject
    public MonitorAction(ApplicationConfiguration config) {
        super(config);
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String changeStatus() {
        setJavaMelodySystemProperty();

        redirectURL = httpRequest.getHeader("Referer");
        return "redirect";
    }

    private void setJavaMelodySystemProperty() {
        System.setProperty(ApplicationConfiguration.JAVA_MELODY_SYSTEM_PROP, Boolean.toString(enabled));

        if (logger.isInfoEnabled()) {
            logger.info("JavaMelody monitoring {}.", enabled ? "ENABLED" : "DISABLED");
        }
    }

    public String getRedirectURL() {
        return redirectURL;
    }
}
