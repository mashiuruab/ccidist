package com.cefalo.cci.module;

import com.cefalo.cci.action.DateConverter;
import com.cefalo.cci.mapping.AbstractJerseyServletModule;
import com.cefalo.cci.restResource.*;
import com.google.common.collect.ImmutableMap;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import org.apache.shiro.guice.web.ShiroWebModule;

import java.util.Map;

public class AdminJerseyServletModule extends AbstractJerseyServletModule {
    @Override
    public ImmutableMap<String, String> getCustomJerseyServletParams() {
        return ImmutableMap.of(ServletContainer.PROPERTY_WEB_PAGE_CONTENT_REGEX, "/(static|api)/(.)*");
    }

    @Override
    public void configureJerseyResources() {
        bind(RedirectOrgListResource.class);
        bind(AdminIssueResource.class);
        bind(AccessTokenResource.class);
        bind(ContentResource.class);
        bind(VersionResource.class);

        // FIXME: Isn't this a struts thing??? Why the hell is this here???
        bind(DateConverter.class);
    }

    @Override
    public void configureFilterResource(Map<String, String> params) {
        ShiroWebModule.bindGuiceFilter(binder());
        filter("/*").through(GuiceContainer.class, params);
    }
}
