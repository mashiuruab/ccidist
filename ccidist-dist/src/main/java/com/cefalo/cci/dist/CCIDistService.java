package com.cefalo.cci.dist;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.webapp.WebAppContext;
import org.mortbay.setuid.SetUIDServer;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author partha
 *
 */
public class CCIDistService {
    private static final String PROPERTY_CCIDIST_DIR = "com.ccidist.dir";
    private static final String PROPERTY_HTTP_PORT = "com.ccidist.port";
    private static final String PROPERTY_USERNAME = "com.ccidist.user";
    private static final String PROPERTY_GROUPNAME = "com.ccidist.group";

    private static final String DEFAULT_HTTP_PORT = "8080";
    private static final String DEFAULT_USER = "ccidist";
    private static final String DEFAULT_GROUP = "ccidist";
    private static final String DEFAULT_CCIDIST_DIR = ".";

    private static SetUIDServer server = null;

    public static void main(String[] args) throws Exception {
        configureJetty();
    }

    private static void configureJetty() throws Exception {
        // SetUIDServer needs root privilege to start. However, it will switch to a normal user once the privileged
        // ports are registered.
        server = new SetUIDServer();

        // Set username and password for running jetty
        server.setUsername(System.getProperty(PROPERTY_USERNAME, DEFAULT_USER));
        server.setGroupname(System.getProperty(PROPERTY_GROUPNAME, DEFAULT_GROUP));

        int port = Integer.parseInt(System.getProperty(PROPERTY_HTTP_PORT, DEFAULT_HTTP_PORT));

        Connector connector = new SelectChannelConnector();
        connector.setPort(port);
        server.addConnector(connector);

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        addWar(contexts, "/admin", "ccidist-admin.war");
        addWar(contexts, "/webservice", "ccidist-ws.war");
        addWar(contexts, "/ccidist-digitaldriver", "ccidist-digitaldriver.war");

        contexts.addLifeCycleListener(new AbstractLifeCycle.AbstractLifeCycleListener() {
            @Override
            public void lifeCycleStarted(LifeCycle event) {
                // This message is checked by the init scripts. If you change this, change the init script too.
                System.err.println("CCI Distribution Service is UP :-)");
            }
        });

        server.setHandler(contexts);
        server.start();
        server.setStopAtShutdown(true);
    }

    private static void addWar(ContextHandlerCollection contexts, String contextPath, String name) throws Exception {
        String basePath = System.getProperty(PROPERTY_CCIDIST_DIR, DEFAULT_CCIDIST_DIR);

        WebAppContext wac = new WebAppContext();
        wac.setServer(server);
        wac.setContextPath(contextPath);
        wac.setWar(basePath + "/webapps/" + name);
        addDBDriver(basePath, wac);
        /*
         * Here setParentLoaderPriority is used as true to load resource properties by jetty as compatible with tomcat
         * or weblogic. Jetty will load resource properties from it's classpath instead of war classpath.
         */
        wac.setParentLoaderPriority(true);
        contexts.addHandler(wac);
    }

    private static void addDBDriver(String basePath, WebAppContext wac) {
        File[] fileList = new File(basePath, "/database/driver/").listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String fileName) {
                return "jar".equalsIgnoreCase(getExtension(fileName));
            }

            private String getExtension(String fileName) {
                String extension = "";
                int i = fileName.lastIndexOf('.');
                if (i > 0 && i != fileName.length() - 1) {
                    extension = fileName.substring(i + 1);
                }
                return extension;
            }

        });

        if (fileList == null || fileList.length == 0) {
            throw new RuntimeException("No Database Driver found. We can't proceed.");
        }

        StringBuilder extraClasspath = new StringBuilder();
        for (int i = 0; i < fileList.length; ++i) {
            extraClasspath.append(fileList[i].getAbsolutePath());
            if (i != fileList.length - 1) {
                extraClasspath.append(":");
            }
        }

        System.out.println("Extra classpath for DB drivers: " + extraClasspath.toString());
        wac.setExtraClasspath(extraClasspath.toString());
    }
}
