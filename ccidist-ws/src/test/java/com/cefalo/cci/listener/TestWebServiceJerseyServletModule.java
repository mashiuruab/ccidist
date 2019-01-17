package com.cefalo.cci.listener;

import com.cefalo.cci.utils.locator.ResourceLocator;
import com.cefalo.cci.mapping.locator.AdminWebappLocatorProvider;
import com.cefalo.cci.mapping.locator.DigitaldriverWebappLocatorProvider;
import com.cefalo.cci.mapping.locator.ExternalAdminWebappLocatorProvider;
import com.cefalo.cci.mapping.locator.WebserviceResourceLocatorProvider;
import com.cefalo.cci.restResource.*;
import com.google.common.collect.ImmutableMap;
import com.google.inject.name.Names;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import org.apache.shiro.guice.web.ShiroWebModule;

import java.util.Map;

import static com.sun.jersey.api.core.ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS;
import static com.sun.jersey.api.core.ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS;

public class TestWebServiceJerseyServletModule extends TestJerseyServletModule {

    @Override
    public void configureJerseyResources() {
        // These are the resources required for the webservice.
        bind(OrganizationResource.class);
        bind(PublicationResource.class);
        bind(IssueResource.class);
        bind(AtomFeedSupport.class);
        bind(AdminIssueResource.class);
        bind(AccessTokenResource.class);
        bind(ContentResource.class);

        // The locators
        bind(ResourceLocator.class).annotatedWith(Names.named("webservice")).toProvider(
                WebserviceResourceLocatorProvider.class);
        bind(ResourceLocator.class).annotatedWith(Names.named("admin")).toProvider(AdminWebappLocatorProvider.class);
        bind(ResourceLocator.class).annotatedWith(Names.named("externalAdmin")).toProvider(
                ExternalAdminWebappLocatorProvider.class);
        bind(ResourceLocator.class).annotatedWith(Names.named("digitaldriver")).toProvider(
                DigitaldriverWebappLocatorProvider.class);
    }

    @Override
    public ImmutableMap<String, String> getCustomJerseyServletParams() {
        return ImmutableMap.of(PROPERTY_CONTAINER_REQUEST_FILTERS, "com.cefalo.cci.filter.AuthRequestFilter",
                PROPERTY_CONTAINER_RESPONSE_FILTERS, "com.cefalo.cci.filter.AuthResponseFilter");
    }

    @Override
    public void configureFilterResource(Map<String, String> params) {
        ShiroWebModule.bindGuiceFilter(binder());
        filter("/*").through(GuiceContainer.class, params);
    }
}
