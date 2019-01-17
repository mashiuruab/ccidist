package com.cefalo.cci.action;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.model.Users;
import com.opensymphony.xwork2.ActionSupport;

public abstract class AuthenticatedActionSupport extends ActionSupport implements ServletRequestAware {
    private static final long serialVersionUID = 7157150370134805778L;

    private final Logger logger = LoggerFactory.getLogger(AuthenticatedActionSupport.class);

    protected HttpServletRequest httpRequest;
    protected ApplicationConfiguration config;
    protected String pagetTitle = "Oh Snap!!!";

    public AuthenticatedActionSupport(ApplicationConfiguration config) {
        this.config = config;
    }

    @Override
    public void validate() {
        super.validate();

        if (!isAuthenticated()) {
            logger.error("Unauthenticated access attempt for {}", getClass().getName());
            throw new UnauthenticatedException(getText("unauthenticated.access"));
        }
    }

    public boolean isSuperUser() {
        return isAuthenticated() && Users.SUPER_USER_NAME.equals(getLoggedInUser().getLoginName());
    }

    protected boolean isAuthenticated() {
        return getLoggedInUser() != null;
    }

    public Users getLoggedInUser() {
        return SecurityUtils.getSubject().getPrincipals().oneByType(Users.class);
    }

    public String getPageTitle() {
        return pagetTitle;
    }

    public void setPagetTitle(String pagetTitle) {
        this.pagetTitle = pagetTitle;
    }

    @Override
    public void setServletRequest(HttpServletRequest request) {
        this.httpRequest = request;
    }

    protected String getRequestMethod() {
        return httpRequest.getMethod();
    }

    public ApplicationConfiguration getConfig() {
        return config;
    }
}
