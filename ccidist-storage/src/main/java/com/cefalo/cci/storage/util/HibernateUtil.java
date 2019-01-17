/**
 *
 */
package com.cefalo.cci.storage.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;

import javax.persistence.EntityManager;

import org.hibernate.Session;


public abstract class HibernateUtil {
    /**
     * Just make it a proper "util" class.
     */
    private HibernateUtil() {

    }

    public static Blob getBlob(final EntityManager entityManager, final InputStream dataStream) throws IOException {
        Session session = (Session) entityManager.getDelegate();
        int length = dataStream.available();
        return session.getLobHelper().createBlob(new AutoCloseInputStream(dataStream, length), length);
    }

    public static Object getEntityId(Object entity, EntityManager entityManager) {
        if (entity == null) {
            return "UNKNOWN";
        }

        Session session = (Session) entityManager.getDelegate();
        return session.getIdentifier(entity);
    }
}
