package com.cefalo.cci.dao;

import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.event.CacheEventListenerFactory;

/**
 * This will log cache events only when DEBUG level is enabled. It is referenced in the ehcache.xml file.
 *
 * @author partha
 *
 */
public class LoggingCacheListenerFactory extends CacheEventListenerFactory {
    private final Logger logger = LoggerFactory.getLogger(LoggingCacheListenerFactory.class);

    @Override
    public CacheEventListener createCacheEventListener(Properties properties) {
        return new CacheEventListener() {
            @Override
            public void notifyElementRemoved(Ehcache cache, Element element) throws CacheException {
                log(cache, element, "REMOVE");
            }

            @Override
            public void notifyElementPut(Ehcache cache, Element element) throws CacheException {
                log(cache, element, "PUT");
            }

            @Override
            public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException {
                log(cache, element, "UPDATE");

            }

            @Override
            public void notifyElementExpired(Ehcache cache, Element element) {
                log(cache, element, "EXPIRED");
            }

            @Override
            public void notifyElementEvicted(Ehcache cache, Element element) {
                log(cache, element, "EVICTED");
            }

            @Override
            public void notifyRemoveAll(Ehcache cache) {
                if (logger.isDebugEnabled()) {
                    logger.debug("All entries removed from cache: {}", cache.getName());
                }
            }

            @Override
            public void dispose() {
                if (logger.isDebugEnabled()) {
                    logger.debug("Cache listener disposed.");
                }
            }

            @Override
            public Object clone() throws CloneNotSupportedException {
                return super.clone();
            }
        };
    }

    private void log(Ehcache cache, Element element, String operationName) {
        if (!logger.isDebugEnabled()) {
            return;
        }

        StringBuilder logMessageBuilder = new StringBuilder();
        logMessageBuilder.append(String.format("Cache: %s, OP: %s --> Key: %s. Value: %s.", cache.getName(),
                operationName, element.getObjectKey(), element.getObjectValue()));
        if (element.getObjectValue() instanceof List) {
            logMessageBuilder.append(String.format(" Value is List with size: %s.",
                    ((List<?>) element.getObjectValue()).size()));
        }
        logger.debug(logMessageBuilder.toString());

        if (logger.isTraceEnabled()) {
            logger.trace("Stats: {}", cache.getStatistics());
        }
    }
}
