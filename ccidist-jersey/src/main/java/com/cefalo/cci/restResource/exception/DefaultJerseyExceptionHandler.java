package com.cefalo.cci.restResource.exception;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cefalo.cci.storage.util.HibernateUtil;
import com.google.inject.Singleton;

@Provider
@Singleton
public class DefaultJerseyExceptionHandler implements ExceptionMapper<Exception> {
    private final Logger logger = LoggerFactory.getLogger(DefaultJerseyExceptionHandler.class);

    @Inject
    private javax.inject.Provider<EntityManager> emProvider;

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof WebApplicationException) {
            logger.error("Web app exception.", exception);
            return ((WebApplicationException) exception).getResponse();
        } else if (exception instanceof OptimisticLockException) {
            String errorMessage = null;
            try {
                OptimisticLockException ole = (OptimisticLockException) exception;
                Object entity = ole.getEntity();

                errorMessage = String.format(
                        "Version Conflict, possibly for concurrent updates. Object: %s, ID: %s",
                        entity,
                        HibernateUtil.getEntityId(entity, emProvider.get()));
            } catch (Exception ex) {
                errorMessage = "Version conflict in DB operations.";
            }

            logger.error(errorMessage, exception);
            return Response
                    .status(Status.CONFLICT)
                    .entity(errorMessage)
                    .build();
        }

        // First log this
        logger.error("An unhandled error in rest resource: {}", uriInfo.getAbsolutePath(), exception);
        // Return a 500 error
        return Response.serverError().entity(exception.getMessage()).build();
    }

}
