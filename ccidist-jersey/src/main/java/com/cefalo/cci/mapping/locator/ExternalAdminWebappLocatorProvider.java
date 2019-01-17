package com.cefalo.cci.mapping.locator;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class ExternalAdminWebappLocatorProvider extends AbstractResourceLocatorProvider {
    @Inject
    public ExternalAdminWebappLocatorProvider(ApplicationConfiguration config) {
        super(Strings.isNullOrEmpty(config.getExternalAdminURL()) ? config.getAdminURL() : config.getExternalAdminURL());
    }
}
