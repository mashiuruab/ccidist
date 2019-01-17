package com.cefalo.cci.mapping.locator;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.google.inject.Inject;

public class AdminWebappLocatorProvider extends AbstractResourceLocatorProvider {
    @Inject
    public AdminWebappLocatorProvider(ApplicationConfiguration config) {
        super(config.getAdminURL());
    }
}
