package com.cefalo.cci.listener;

import java.util.HashMap;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public abstract class TestJerseyServletModule extends com.sun.jersey.guice.JerseyServletModule {

    @Override
    protected void configureServlets() {
        configureJerseyResources();

        Map<String, String> params = new HashMap<String, String>();
        params.put(ServletContainer.JSP_TEMPLATES_BASE_PATH, "/WEB-INF/jsp");
        params.put(ServletContainer.PROPERTY_WEB_PAGE_CONTENT_REGEX, "/(struts|static)/.*");

        params.put(PackagesResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, "com.cefalo.cci.filter.AuthRequestFilter");
        params.put(PackagesResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                "com.cefalo.cci.filter.AuthResponseFilter");

        params.putAll(getCustomJerseyServletParams());
        configureFilterResource(params);
    }

    public abstract ImmutableMap<String, String> getCustomJerseyServletParams();

    public abstract void configureJerseyResources();

    public abstract void configureFilterResource(Map<String, String> params);

}
