package com.cefalo.cci.mapping.locator;

import com.cefalo.cci.config.ApplicationConfiguration;
import javax.inject.Inject;

public class WebserviceResourceLocatorProvider extends AbstractResourceLocatorProvider {
    @Inject
    public WebserviceResourceLocatorProvider(ApplicationConfiguration config) {
        super(config.getWebserviceURL());
    }
}
