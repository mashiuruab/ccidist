package com.cefalo.cci.dist;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.webapp.WebAppContext;

import com.google.common.io.Files;

/**
 *
 * @author partha
 *
 */
public class DevelopmentServer {
    private static final String WEBSERVICE_MODULE = "ccidist-ws";
    private static final String ADMIN_WEBAPP_MODULE = "ccidist-admin";

    private static Server server = null;

    public static void main(String[] args) throws Exception {
        System.out.println("****************************************************************");
        System.out.println("************ I HOPE YOU KNOW WHAT YOU ARE DOING ;-) ************");
        System.out.println("****************************************************************");

        configureJetty();
    }

    private static void configureJetty() throws Exception {
        server = new Server();

        Connector connector = new SelectChannelConnector();
        connector.setPort(8080);
        server.addConnector(connector);

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.addLifeCycleListener(new AbstractLifeCycle.AbstractLifeCycleListener() {
            @Override
            public void lifeCycleStarted(LifeCycle event) {
                // This message is checked by the init scripts. If you change this, change the init script too.
                System.err.println("CCI Distribution Service is UP :-)");
            }
        });

        addWars(contexts);

        server.setHandler(contexts);
        server.start();
        server.setStopAtShutdown(true);
    }

    private static void addWar(ContextHandlerCollection contexts, File baseDirectory, String contextPath, String name)
            throws Exception {
        WebAppContext wac = new WebAppContext();
        wac.setServer(server);
        wac.setContextPath(contextPath);
        wac.setWar(findWar(baseDirectory, name));
        wac.setParentLoaderPriority(false);
        contexts.addHandler(wac);
    }

    private static String findWar(final File baseDirectory, final String moduleName) throws Exception {
        File moduleTargetDir = new File(baseDirectory, moduleName.concat("/target"));
        File[] wars = moduleTargetDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(moduleName) && "war".equals(Files.getFileExtension(name));
            }
        });

        if (wars == null || wars.length == 0) {
            throw new RuntimeException("I could not find the war file. Did you run 'mvn install' before this??");
        }

        return wars[0].getAbsolutePath();
    }

    private static void addWars(ContextHandlerCollection contexts) throws Exception {
        File rootDirectory = findRootDirectory();
        System.out.println("Root directory is: " + rootDirectory.getAbsolutePath());

        addWar(contexts, rootDirectory, "/admin", ADMIN_WEBAPP_MODULE);
        addWar(contexts, rootDirectory, "/webservice", WEBSERVICE_MODULE);
    }

    private static File findRootDirectory() {
        // Eclipse uses ".." but IDEA uses ".". Sighhhhhh....
        for (String path : Arrays.asList(".", "..")) {
            File directory = new File(path, ADMIN_WEBAPP_MODULE);
            if (directory.exists() && directory.isDirectory()) {
                return new File(path);
            }
        }

        throw new RuntimeException("I can only run from default config of Eclipse or IDEA. Sorry :-(");
    }
}
