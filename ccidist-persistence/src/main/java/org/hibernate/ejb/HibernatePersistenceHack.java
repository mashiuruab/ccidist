package org.hibernate.ejb;

import com.cefalo.cci.utils.PropertyUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;

import java.util.Map;
import java.util.Properties;

public class HibernatePersistenceHack extends HibernatePersistence {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static EntityManagerFactory emf = null;

    @Override
    @SuppressWarnings({ "deprecation", "rawtypes" })
    public EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, Map properties) {
        if (emf != null) {
            logger.info("Returning an EMF to application that was created for the container.");
            return emf;
        }

        logger.info("Will create an EMF for the application.");

        Ejb3ConfigurationHack cfg = new Ejb3ConfigurationHack();

        Ejb3Configuration configured = cfg.configure(persistenceUnitName, properties);

        return configured != null ? configured.buildEntityManagerFactory() : null;
    }

    @Override
    public EntityManagerFactory createContainerEntityManagerFactory(
            PersistenceUnitInfo info,
            @SuppressWarnings("rawtypes") Map properties) {
        logger.info("Going to create an EMF for the container.");

        Properties props = PropertyUtils.readPropertiesFile("/Application.properties");
        props = PropertyUtils.processAdvancedHibernateProperties(props);
        info.getProperties().putAll(props);

        emf = super.createContainerEntityManagerFactory(info, properties);
        return emf;
    }
}
