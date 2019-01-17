package com.cefalo.cci.listener;

import com.cefalo.cci.event.listener.EventListener;
import com.cefalo.cci.event.manager.EventManager;
import com.cefalo.cci.mapping.ApplicationServicesModule;
import com.cefalo.cci.mapping.EpubStoragePrivateModule;
import com.cefalo.cci.mapping.RxmlStoragePrivateModule;
import com.cefalo.cci.module.AdminJerseyServletModule;
import com.cefalo.cci.module.CciShiroWebModule;
import com.cefalo.cci.module.Struts2Module;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.struts2.Struts2GuicePluginModule;

import org.apache.shiro.guice.aop.ShiroAopModule;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

public class AdminContextListener extends CommonServletContextListener {
    private ServletContext servletContext;
    private Injector injector = null;

    @Override
    protected Injector getInjector() {
        // IMPORTANT: The order of the modules do matter here. Only change the order if you know exactly what you are
        // doing.
        injector = Guice.createInjector(
                /** Sets up our application services. This also sets up JPA.
                 * Must be defined before any module that requires JPA. **/
                new ApplicationServicesModule(),
                new EpubStoragePrivateModule(),
                new RxmlStoragePrivateModule(),
                new CciShiroWebModule(servletContext),
                new ShiroAopModule(),
                new Struts2Module(), // Sets up struts filters
                new Struts2GuicePluginModule(), // Default struts2 module
                new AdminJerseyServletModule()); // Sets up jersey resources

        EventManager eventManager = injector.getInstance(EventManager.class);
        EventListener listener = injector.getInstance(EventListener.class);
        eventManager.addListener(listener);

        eventManager.doStart();

        return injector;
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        servletContext = servletContextEvent.getServletContext();
        super.contextInitialized(servletContextEvent);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        try {
            stopEventManagerThread();
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.contextDestroyed(servletContextEvent);
    }

    private void stopEventManagerThread() {
        if (injector != null) {
            EventManager eventManager = injector.getInstance(EventManager.class);
            eventManager.doStop();
        }
    }
}
