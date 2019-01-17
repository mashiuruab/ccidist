package com.cefalo.cci.listener;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.shiro.guice.aop.ShiroAopModule;

import com.cefalo.cci.event.listener.EventListener;
import com.cefalo.cci.event.manager.EventManager;
import com.cefalo.cci.mapping.EpubStoragePrivateModule;
import com.cefalo.cci.mapping.RxmlStoragePrivateModule;
import com.cefalo.cci.module.TestCciShiroWebModule;
import com.cefalo.cci.utils.FileUtils;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

public class TestServletContextListener extends GuiceServletContextListener {
    private static final String TMPDIR_PROPERTY = "java.io.tmpdir";

    private ServletContext servletContext;

    @Override
    protected Injector getInjector() {
        // Setup the CacheStorage directory for integration tests.
        File cacheDir = createDirectoryInTmp("ccidist_integration_test");

        // Setup a custom /tmp for the integration tests
        File tmpPath = createDirectoryInTmp("ccidist_tmp");
        System.setProperty(TMPDIR_PROPERTY, tmpPath.getAbsolutePath());


        Injector injector = Guice.createInjector(
                new TestApplicationServicesModule(cacheDir),
                new EpubStoragePrivateModule(),
                new RxmlStoragePrivateModule(),
                new TestCciShiroWebModule(servletContext),
                new ShiroAopModule(),
                new TestWebServiceJerseyServletModule()
        );

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

    private File createDirectoryInTmp(String dirName) {
        File newDir = new File(System.getProperty(TMPDIR_PROPERTY), dirName);
        FileUtils.deleteRecursive(newDir);
        newDir.mkdirs();

        return newDir;
    }
}
