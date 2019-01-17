package com.cefalo.cci.mapping;

import com.cefalo.cci.restResource.*;
import com.google.common.collect.ImmutableMap;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import java.util.Map;

import static com.sun.jersey.api.core.PackagesResourceConfig.*;

public class WebserviceJerseyServletModule extends AbstractJerseyServletModule {

    @Override
    public void configureJerseyResources() {
        // These are the resources required for the webservice.
        bind(OrganizationResource.class);
        bind(PublicationResource.class);
        bind(IssueResource.class);
        bind(AtomFeedSupport.class);
    }

    @Override
    public ImmutableMap<String, String> getCustomJerseyServletParams() {
        return ImmutableMap.of(PROPERTY_CONTAINER_REQUEST_FILTERS, "com.cefalo.cci.filter.AuthRequestFilter",
                PROPERTY_CONTAINER_RESPONSE_FILTERS, "com.cefalo.cci.filter.AuthResponseFilter");
    }

    @Override
    public void configureFilterResource(Map<String, String> params) {
       filter("/*").through(GuiceContainer.class, params);
    }
}
