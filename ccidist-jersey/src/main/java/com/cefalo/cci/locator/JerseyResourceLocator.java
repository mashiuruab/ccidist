package com.cefalo.cci.locator;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.UriBuilder;

import com.cefalo.cci.restResource.AccessTokenResource;
import com.cefalo.cci.restResource.AdminIssueResource;
import com.cefalo.cci.restResource.ContentResource;
import com.cefalo.cci.restResource.IssueResource;
import com.cefalo.cci.restResource.OrganizationResource;
import com.cefalo.cci.restResource.PublicationResource;
import com.cefalo.cci.utils.locator.ResourceLocator;

public class JerseyResourceLocator implements ResourceLocator {
    private final URI uri;
    private final Map<String, String> queryParameters = new LinkedHashMap<>();

    public JerseyResourceLocator(final URI uri) {
        this.uri = uri;
    }

    @Override
    public URI getOrganizationListURI() {
        return createUriBuilder(uri).path(OrganizationResource.class).build();
    }

    @Override
    public URI getOrganizationURI(final String organizationID) {
        checkNotNull(organizationID);
        return createUriBuilder(uri).path(OrganizationResource.class)
                .path(OrganizationResource.class, "getOrganizationDetail").build(organizationID);
    }

    @Override
    public URI getPublicationURI(
            final String organizationID,
            final String publicationID) {
        checkNotNull(organizationID);
        checkNotNull(publicationID);
        return createUriBuilder(uri).path(PublicationResource.class).build(organizationID, publicationID);
    }

    @Override
    public URI getIssueListURI(
            final String organizationID,
            final String publicationID) {
        checkNotNull(organizationID);
        checkNotNull(publicationID);
        return createUriBuilder(uri).path(IssueResource.class).build(organizationID, publicationID);
    }

    @Override
    public URI getIssueURI(
            final String organizationID,
            final String publicationID,
            final long issueID) {
        checkNotNull(organizationID);
        checkNotNull(publicationID);
        checkNotNull(issueID);

        return createUriBuilder(uri).path(IssueResource.class).path(IssueResource.class, "getIssueDetail")
                .build(organizationID, publicationID, issueID);
    }

    @Override
    public URI getEventQueueURI(final String organizationId,
            final String publicationId,
            final long issueId) {
        checkNotNull(organizationId);
        checkNotNull(publicationId);
        checkNotNull(issueId);

        return createUriBuilder(uri).path(IssueResource.class).path(IssueResource.class, "getEventQueue")
                .build(organizationId, publicationId, issueId);
    }

    @Override
    public URI getEpubBinaryURI(
            final String organizationID,
            final String publicationID,
            final long issueID) {
        checkNotNull(organizationID);
        checkNotNull(publicationID);
        checkNotNull(issueID);

        return createUriBuilder(uri).path(IssueResource.class).path(IssueResource.class, "downloadEpub")
                .build(organizationID, publicationID, issueID);
    }

    @Override
    public URI getEpubContentURI(
            final String organizationID,
            final String publicationID,
            final long issueID,
            final String contentLocation) {
        checkNotNull(organizationID);
        checkNotNull(publicationID);
        checkNotNull(issueID);
        checkNotNull(contentLocation);

        return createUriBuilder(uri).path(IssueResource.class).path(IssueResource.class, "getEpubContent")
                .build(organizationID, publicationID, issueID, contentLocation);
    }

    @Override
    public URI getTokenURI(
            final String organizationId,
            final String publicationId) {
        return createUriBuilder(uri).path(AccessTokenResource.class).build(organizationId, publicationId);

    }

    @Override
    public URI getMatchedContentURI(
            final String organizationId,
            final String publicationId) {
        return createUriBuilder(uri).path(ContentResource.class).build(organizationId, publicationId);
    }

    @Override
    public URI getCCIObjectXMLUploadURI(
            final String organizationId,
            final String publicationId) {
        return createUriBuilder(uri).path(AdminIssueResource.class).path(AdminIssueResource.class, "uploadRxmlFile")
                .build(organizationId, publicationId);
    }

    @Override
    public URI getPublicIssueDetailURI(
            final String organizationId,
            final String publicationId,
            final long issueId) {
        checkNotNull(organizationId);
        checkNotNull(publicationId);
        checkNotNull(issueId);

        return createUriBuilder(uri).path(IssueResource.class).path(IssueResource.class, "getPublicIssueDetail")
                .build(organizationId, publicationId, issueId);
    }

    @Override
    public URI getURI() {
        return createUriBuilder(uri).build();
    }


    private UriBuilder createUriBuilder(URI uri) {
        UriBuilder builder = UriBuilder.fromUri(uri);
        for (Entry<String, String> entry : queryParameters.entrySet()) {
            builder.queryParam(entry.getKey(), entry.getValue());
        }
        return builder;
    }

    @Override
    public ResourceLocator addQueryParameter(String parameterName, String parameterValue) {
        // NOTE: Apparently URIBuilder won't escape "," in the query param. But JDK URLEncode.encode does. It
        // seems that the JDK is right. So, we'll use that.
        try {
            queryParameters.put(checkNotNull(parameterName), URLEncoder.encode(checkNotNull(parameterValue), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // UTF-8 is guaranteed. So, we don't handle this exception.
        }
        return this;
    }
}
