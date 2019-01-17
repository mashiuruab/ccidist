package com.cefalo.cci.action.admin;

import org.apache.shiro.authz.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cefalo.cci.action.AuthenticatedActionSupport;
import com.cefalo.cci.config.ApplicationConfiguration;

public abstract class AdministrativeActionSupport extends AuthenticatedActionSupport {
    private static final long serialVersionUID = 1713243230341059749L;

    private final Logger logger = LoggerFactory.getLogger(AdministrativeActionSupport.class);

    public AdministrativeActionSupport(ApplicationConfiguration config) {
        super(config);
    }

    @Override
    public void validate() {
        super.validate();

        if (!isSuperUser()) {
            logger.error("Only 'admin' user can access {}", getClass().getName());
            throw new UnauthorizedException(getText("unauthorized.access"));
        }
    }
}
