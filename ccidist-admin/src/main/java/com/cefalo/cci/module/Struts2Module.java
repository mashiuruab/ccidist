package com.cefalo.cci.module;

import org.apache.shiro.guice.web.ShiroWebModule;
import org.apache.struts2.dispatcher.ng.filter.StrutsPrepareAndExecuteFilter;

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;

public class Struts2Module extends ServletModule {
    @Override
    protected void configureServlets() {
        bind(StrutsPrepareAndExecuteFilter.class).in(Scopes.SINGLETON);

        ShiroWebModule.bindGuiceFilter(binder());

        // All the actions are mapped to *.action.
        filterRegex("(.)*\\.action").through(StrutsPrepareAndExecuteFilter.class);

        // Bootstrap theme related static files are generated under /struts/. So that has to go through the struts
        // filter too.
        filterRegex("/struts/(.)*").through(StrutsPrepareAndExecuteFilter.class);
    }
}
