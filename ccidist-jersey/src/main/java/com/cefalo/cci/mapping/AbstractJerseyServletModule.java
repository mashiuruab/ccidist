package com.cefalo.cci.mapping;

import java.util.HashMap;
import java.util.Map;

import com.cefalo.cci.mapping.locator.AdminWebappLocatorProvider;
import com.cefalo.cci.mapping.locator.ExternalAdminWebappLocatorProvider;
import com.cefalo.cci.mapping.locator.DigitaldriverWebappLocatorProvider;
import com.cefalo.cci.mapping.locator.WebserviceResourceLocatorProvider;
import com.cefalo.cci.restResource.exception.DefaultJerseyExceptionHandler;
import com.cefalo.cci.utils.locator.ResourceLocator;
import com.google.common.collect.ImmutableMap;
import com.google.inject.name.Names;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public abstract class AbstractJerseyServletModule extends JerseyServletModule {

    @Override
    protected void configureServlets() {
        configureJerseyResources();

        // Bind the default Exception mapper
        bind(DefaultJerseyExceptionHandler.class);

        // Bind the locators
        bind(ResourceLocator.class).annotatedWith(Names.named("webservice")).toProvider(
                WebserviceResourceLocatorProvider.class);
        bind(ResourceLocator.class).annotatedWith(Names.named("admin")).toProvider(AdminWebappLocatorProvider.class);
        bind(ResourceLocator.class).annotatedWith(Names.named("externalAdmin")).toProvider(
                ExternalAdminWebappLocatorProvider.class);
        bind(ResourceLocator.class).annotatedWith(Names.named("digitaldriver")).toProvider(
                DigitaldriverWebappLocatorProvider.class);

        Map<String, String> params = new HashMap<String, String>();
        params.put(ServletContainer.JSP_TEMPLATES_BASE_PATH, "/WEB-INF/jsp");

        params.putAll(getCustomJerseyServletParams());
        configureFilterResource(params);
    }

    public abstract ImmutableMap<String, String> getCustomJerseyServletParams();

    public abstract void configureJerseyResources();

    public abstract void configureFilterResource(Map<String, String> params);
}
