package com.cefalo.cci.utils.locator;

import java.net.URI;

/**
 * FIXME: This is awkward! To get the URI of something, we need to know the ID of all its parent resources. We need to
 * revisit our URI scheme and resource content.
 *
 * @author partha
 *
 */
public interface ResourceLocator {
    URI getOrganizationListURI();

    URI getOrganizationURI(String organizationID);

    URI getPublicationURI(
            String organizationID,
            String publicationID);

    URI getIssueListURI(
            String organizationID,
            String publicationID);

    URI getIssueURI(
            String organizationID,
            String publicationID,
            long issueID);

    URI getEventQueueURI(
            String organizationId,
            String publicationId,
            long issueId);

    URI getEpubBinaryURI(
            String organizationID,
            String publicationID,
            long issueID);

    URI getEpubContentURI(
            String organizationID,
            String publicationID,
            long issueID,
            String contentLocation);

    URI getTokenURI(
            String organizationId,
            String publicationId);

    URI getMatchedContentURI(
            String organizationId,
            String publicationId);

    URI getCCIObjectXMLUploadURI(
            String organizationId,
            String publicationId);

    URI getPublicIssueDetailURI(
            String organizationId,
            String publicationId,
            long issueId);

    URI getURI();

    /**
     * Adds a query parameter to the generated URL.
     *
     * @param parameterName
     *            may not be <code>null</code>
     * @param parameterValue
     *            may not be <code>null</code>. This also should not be URL encoded. This method will do that.
     * @return reference to this object to support fluent method calls.
     */
    ResourceLocator addQueryParameter(String parameterName, String parameterValue);
}
