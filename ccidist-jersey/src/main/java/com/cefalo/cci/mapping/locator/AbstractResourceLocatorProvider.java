package com.cefalo.cci.mapping.locator;

import java.net.URI;

import com.cefalo.cci.locator.JerseyResourceLocator;
import com.cefalo.cci.utils.locator.ResourceLocator;
import com.google.inject.Provider;

public class AbstractResourceLocatorProvider implements Provider<ResourceLocator> {
    private final URI locatorURI;

    public AbstractResourceLocatorProvider(final String locatorURL) {
        this.locatorURI = URI.create(locatorURL);
    }

    @Override
    public ResourceLocator get() {
        return new JerseyResourceLocator(locatorURI);
    }
}
