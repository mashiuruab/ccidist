package com.cefalo.cci.listener;

import com.google.inject.servlet.GuiceServletContextListener;

import javax.servlet.ServletContextEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public abstract class CommonServletContextListener extends GuiceServletContextListener {
    private final Logger logger = LoggerFactory.getLogger(CommonServletContextListener.class);

    public CommonServletContextListener() {
        super();
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        super.contextInitialized(servletContextEvent);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        try {
            forciblyDeRegisterJDBCDrivers();
            removeKnownThreads();
            removeThreadLocals();
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        } catch (Exception ex) {
            logger.warn("Problem trying to fix memory leaks.", ex);
        }

        super.contextDestroyed(servletContextEvent);
    }

    private void forciblyDeRegisterJDBCDrivers() {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        Driver d = null;
        while (drivers.hasMoreElements()) {
            try {
                d = drivers.nextElement();
                DriverManager.deregisterDriver(d);
            } catch (SQLException ex) {
                logger.warn("Problem while trying to de-register JDBC drivers: {}", d, ex);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void removeKnownThreads() throws Exception {
        // First try to stop the MySQL Abandoned Connection Cleanup thread
        try {
            Class<?> cls = Class.forName("com.mysql.jdbc.AbandonedConnectionCleanupThread");
            Method mth = (cls == null ? null : cls.getMethod("shutdown"));
            if (mth != null) {
                mth.invoke(null);
            }
        } catch (ClassNotFoundException cnfe) {
            // Basically, no MySQL weird thread. We're good.
        }

        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        for (Thread t : threadSet) {
            if (t.getName().contains("Abandoned connection cleanup thread")) {
                synchronized (t) {
                    t.stop();
                }
            }
        }
    }

    private void removeThreadLocals() {
        Thread thread = Thread.currentThread();
        Field threadLocalsField = null;
        try {
            Class<?> threadLocalMapKlazz = Class.forName("java.lang.ThreadLocal$ThreadLocalMap");
            Field tableField = threadLocalMapKlazz.getDeclaredField("table");
            tableField.setAccessible(true);

            threadLocalsField = Thread.class.getDeclaredField("threadLocals");
            threadLocalsField.setAccessible(true);
            Object threadLocals = threadLocalsField.get(thread);

            if (threadLocals != null) {
                Object table = tableField.get(threadLocals);

                // The key to the ThreadLocalMap is a WeakReference object. The referent field of this object
                // is a reference to the actual ThreadLocal variable
                Field referentField = Reference.class.getDeclaredField("referent");
                referentField.setAccessible(true);

                for (int i = 0; i < Array.getLength(table); i++) {
                    // Each entry in the table array of ThreadLocalMap is an Entry object
                    // representing the thread local reference and its value
                    Object entry = Array.get(table, i);
                    if (entry != null) {
                        // Get a reference to the thread local object and remove it from the table
                        ThreadLocal<?> threadLocal = (ThreadLocal<?>) referentField.get(entry);
                        if (threadLocal != null) {
                            if (threadLocal.get() != null
                                    && threadLocal.get().toString().contains("java.lang.ref.WeakReference")) {
                                threadLocal.remove();
                            }
                        }

                    }
                }
            }
        } catch (Exception ex) {
            logger.warn("Problem while trying to remove known ThreadLocals.", ex);
        }
    }
}
