package com.cefalo.cci.mapping.locator;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.google.inject.Inject;

public class DigitaldriverWebappLocatorProvider extends AbstractResourceLocatorProvider {
    @Inject
    public DigitaldriverWebappLocatorProvider(ApplicationConfiguration config) {
        super(config.getDigitaldriverURL());
    }
}
