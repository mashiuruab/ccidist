package com.cefalo.cci.listener;

import com.cefalo.cci.mapping.*;
import net.bull.javamelody.MonitoringGuiceModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.quartz.Scheduler;

import javax.servlet.ServletContextEvent;

public class WebserviceContextListener extends CommonServletContextListener {
    private Injector injector = null;

    @Override
    protected Injector getInjector() {
        // IMPORTANT: The order of the modules do matter here. Only change the order if you know exactly what you are
        // doing.
        injector = Guice.createInjector(
                /**
                 * For Monitoring with Java Melody. This must be the first module since we want to get a proper monitoring
                 * report.
                 **/
                new MonitoringGuiceModule(),
                /**
                 * Sets up our application services. This also sets up JPA.
                 * Must be defined before any module that requires JPA.
                 **/
                new ApplicationServicesModule(),
                new EpubStoragePrivateModule(),
                new RxmlStoragePrivateModule(),
                new WebserviceJerseyServletModule(),  // Sets up jersey stuff
                new PurgeFileQuartzModule()); // Sets up jersey resources
        return injector;
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        super.contextInitialized(servletContextEvent);
        if (injector != null) {
            try {
                getScheduler().start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        try {
            if (injector != null) {
                getScheduler().shutdown(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.contextDestroyed(servletContextEvent);
    }

    private Scheduler getScheduler() {
        return injector.getInstance(Scheduler.class);
    }




}
