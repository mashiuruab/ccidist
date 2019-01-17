package com.cefalo.cci.event.listener;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.net.URI;
import java.util.Map.Entry;

import javax.inject.Named;
import javax.inject.Provider;
import javax.persistence.EntityManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.event.model.Event;
import com.cefalo.cci.event.model.EventType;
import com.cefalo.cci.model.DriverInfo;
import com.cefalo.cci.model.Issue;
import com.cefalo.cci.model.Organization;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.utils.locator.ResourceLocator;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;

public class VarnishListener implements EventListener {
    private static final int THIRTY_SECONDS = (int) SECONDS.toMillis(30);

    private static final String HTTP_BAN = "BAN";
    private static final String HTTP_PURGE = "PURGE";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private ApplicationConfiguration applicationConfiguration;

    @Inject
    private Provider<EntityManager> emProvider;

    @Inject
    @Named("webservice")
    private Provider<ResourceLocator> wsLocator;

    private final HttpClient defaultHttpClient;

    public VarnishListener() {
        defaultHttpClient = createHttpClient();
    }

    @Override
    public void handleEvent(final Event event) {
        if (!applicationConfiguration.skipTokenBasedAuthentication()) {
            // We only do this when the appserver is not supposed to do authentication. Assumption is that if the
            // appserver is not doing authentication, then Varnish is. In any case, even if that is not true, we'll just
            // pay for a INVALID http request since all requests are either BAN or PURGE method.
            return;
        }

        Multimap<String, URI> varnishRequests = convertToVarnishRequests(event);
        for (Entry<String, URI> entry : varnishRequests.entries()) {
            final String requestMethod = entry.getKey();
            final URI requestUri = entry.getValue();

            if (logger.isDebugEnabled()) {
                logger.debug("Will send \"{}\" request to {}", requestMethod, requestUri);
            }

            HttpResponse response = null;
            try {
                response = defaultHttpClient.execute(new HttpGet(requestUri) {
                    @Override
                    public String getMethod() {
                        return requestMethod;
                    }
                });

                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    throw new RuntimeException(
                            String.format(
                                    "Error while trying to communicate with Varnish. URI: %s, Method: %s. Status: %s",
                                    requestUri,
                                    requestMethod,
                                    response.getStatusLine().getStatusCode()));
                } else {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Varnish response for {} request to {} = {}",
                                requestMethod,
                                requestUri,
                                EntityUtils.toString(response.getEntity()));
                    }
                }
            } catch (Exception ex) {
                logger.error("Error while posting events to varnish.", ex);
            } finally {
                if (response != null) {
                    EntityUtils.consumeQuietly(response.getEntity());
                }
            }
        }
    }

    private Multimap<String, URI> convertToVarnishRequests(Event event) {
        if (event.getSourceClass() == Issue.class) {
            return convertIssueEvent(event);
        } else if (event.getSourceClass() == Publication.class) {
            return convertPublicationEvent(event);
        } else if (event.getSourceClass() == Organization.class) {
            return convertOrganizationEvent(event);
        } else if (event.getSourceClass() == DriverInfo.class) {
            return convertDriverInfoEvent(event);
        }

        return ArrayListMultimap.create();
    }

    private Multimap<String, URI> convertIssueEvent(Event event) {
        Multimap<String, URI> varnishRequests = ArrayListMultimap.create();
        ResourceLocator resourceLocator = wsLocator.get();

        long issueId = (long) event.getSourceId();
        Publication publication = null;

        EntityManager entityManager = emProvider.get();
        if (event.getType() == EventType.DELETE) {
            publication = entityManager.find(Publication.class, event.getExtraInfo());
        } else {
            Issue issue = entityManager.find(Issue.class, issueId);
            publication = issue.getPublication();
        }

        URI issueListUri = resourceLocator.getIssueListURI(publication.getOrganization().getId(), publication.getId());
        varnishRequests.put(HTTP_BAN, issueListUri);

        if (event.getType() != EventType.CREATE) {
            URI issueUri = resourceLocator.getIssueURI(
                    publication.getOrganization().getId(),
                    publication.getId(),
                    issueId);
            varnishRequests.put(HTTP_BAN, issueUri);
        }

        return varnishRequests;
    }

    private Multimap<String, URI> convertDriverInfoEvent(Event event) {
        String publicationId = null;
        Object driverId = event.getSourceId();
        if (event.getType() == EventType.DELETE) {
            publicationId = (String) event.getExtraInfo();
        } else {
            DriverInfo driverInfo = emProvider.get().find(DriverInfo.class, driverId);
            publicationId = driverInfo.getPublication().getId();
        }

        // We basically do an update event on publication.
        return convertPublicationEvent(new Event(EventType.UPDATE, Publication.class, publicationId, null));
    }

    private Multimap<String, URI> convertPublicationEvent(Event event) {
        Multimap<String, URI> varnishRequests = ArrayListMultimap.create();
        ResourceLocator resourceLocator = wsLocator.get();

        String publicationId = (String) event.getSourceId();
        String organizationId = null;
        if (event.getType() == EventType.DELETE) {
            organizationId = (String) event.getExtraInfo();
        } else {
            Publication publication = emProvider.get().find(Publication.class, event.getSourceId());
            organizationId = publication.getOrganization().getId();
        }

        URI organizationURI = resourceLocator.getOrganizationURI(organizationId);
        varnishRequests.put(HTTP_PURGE, organizationURI);

        URI publicationUri = resourceLocator.getPublicationURI(organizationId, publicationId);
        if (event.getType() == EventType.DELETE) {
            URI publicationBanUri = URI.create(publicationUri.toString() + "*");
            varnishRequests.put(HTTP_BAN, publicationBanUri);
        } else {
            varnishRequests.put(HTTP_PURGE, publicationUri);
        }

        // Issues contain information of the publication and organization. But we won't invalidate them since the URIs
        // won't change. Only the organization, publication names would change in the issue resource.
        return varnishRequests;
    }

    private Multimap<String, URI> convertOrganizationEvent(Event event) {
        Multimap<String, URI> varnishRequests = ArrayListMultimap.create();
        ResourceLocator resourceLocator = wsLocator.get();

        String organizationId = (String) event.getSourceId();

        URI orgListUri = resourceLocator.getOrganizationListURI();
        varnishRequests.put(HTTP_PURGE, orgListUri);

        URI orgUri = resourceLocator.getOrganizationURI(organizationId);
        if (event.getType() == EventType.DELETE) {
            URI organizationBanUri = URI.create(orgUri.toString() + "*");
            varnishRequests.put(HTTP_BAN, organizationBanUri);
        } else if (event.getType() == EventType.UPDATE) {
            varnishRequests.put(HTTP_PURGE, orgUri);
        }

        return varnishRequests;
    }

    private HttpClient createHttpClient() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultMaxPerRoute(20);
        connectionManager.setMaxTotal(20);

        RequestConfig requestConfig = RequestConfig
                .custom()
                .setConnectionRequestTimeout(THIRTY_SECONDS)
                .setConnectTimeout(THIRTY_SECONDS)
                .setSocketTimeout(THIRTY_SECONDS)
                .build();

        SocketConfig socketConfig = SocketConfig
                .custom()
                .setSoTimeout(THIRTY_SECONDS)
                .build();

        return HttpClients
                .custom()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultSocketConfig(socketConfig)
                .setConnectionManager(connectionManager)
                .build();
    }
}
