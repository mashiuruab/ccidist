package com.cefalo.cci.filter;

import static com.cefalo.cci.filter.AuthRequestFilter.AUTH_TOKEN_NAME;
import static com.cefalo.cci.utils.DateUtils.convertTimeStampWithTZ;
import static com.cefalo.cci.utils.StringUtils.isBlank;
import static com.cefalo.cci.utils.StringUtils.urlDecode;
import static com.cefalo.cci.utils.StringUtils.urlEncode;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.net.URI;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.utils.locator.ResourceLocator;
import com.cefalo.cci.model.Issue;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.model.token.AccessToken;
import com.cefalo.cci.model.token.ProductID;
import com.cefalo.cci.service.IssueService;
import com.cefalo.cci.service.PublicationService;
import com.cefalo.cci.service.TokenGenerator;
import com.google.inject.Provider;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

public class AuthResponseFilter implements ContainerResponseFilter {
    @Inject
    private TokenGenerator tokenGenerator;

    @Inject
    private ApplicationConfiguration config;

    @Inject
    private IssueService issueService;

    @Inject
    private PublicationService publicationService;

    @Inject
    @Named("webservice")
    Provider<ResourceLocator> webserviceLocator;

    @Override
    public ContainerResponse filter(ContainerRequest containerRequest, ContainerResponse containerResponse) {
        if (config.skipTokenBasedAuthentication()) {
            return containerResponse;
        }

        String productId = (String) containerResponse.getHttpHeaders().getFirst(config.getProductIDHeaderName());

        // If there is no product ID, this request does not require any authentication/authorization.
        if (isBlank(productId)) {
            return containerResponse;
        }

        ProductID currentProduct = ProductID.from(productId);
        AccessToken accessToken = (AccessToken) containerRequest.getProperties().get(AUTH_TOKEN_NAME);

        if (accessToken == null
                /* Access token has expired */
                || isExpiredToken(accessToken)
                /* The access token is for a different product */
                || !tokenGenerator.hasAccessToProduct(currentProduct, accessToken)) {
            redirectToLoginUrl(containerRequest);
        }

        // So, we are authorized and all is well. Let's set the cookie from the access token. We don't care if it is
        // there or not.
        NewCookie cookie = new NewCookie(
                AUTH_TOKEN_NAME,
                urlDecode(accessToken.toString()),
                getCookiePath(accessToken.getProductId()),
                null,
                null,
                3600,
                false);
        containerResponse.setResponse(Response.fromResponse(containerResponse.getResponse()).cookie(cookie).build());

        // Remove the product-id header. We don't want others to see it.
        containerResponse.getHttpHeaders().remove(config.getProductIDHeaderName());
        return containerResponse;
    }

    private void redirectToLoginUrl(ContainerRequest containerRequest) {
        // Send users to the login_url
        String redirectLocation = config.getRedirectURLWithQueryKey().concat(
                urlEncode(containerRequest.getAbsolutePath().toString()));
        Response response = Response
                .status(HttpStatus.SC_MOVED_TEMPORARILY)
                .location(URI.create(redirectLocation))
                .build();
        throw new WebApplicationException(response);
    }

    private boolean isExpiredToken(AccessToken token) {
        long currentTime = MILLISECONDS.toSeconds(convertTimeStampWithTZ(currentTimeMillis()));
        return currentTime > token.getTimestamp();
    }

    private String getCookiePath(ProductID product) {
        if (product.getIssueID() == ProductID.WILDCARD_ISSUE_ID) {
            Publication publication = publicationService.getPublication(product.getPublicationID());
            return locator().getIssueListURI(
                    publication.getOrganization().getId(),
                    publication.getId()).toString();
        }

        Issue issue = issueService.getIssue(product.getIssueID());
        return locator().getIssueURI(
                issue.getPublication().getOrganization().getId(),
                issue.getPublication().getId(),
                product.getIssueID()).toString();
    }

    private ResourceLocator locator() {
        return webserviceLocator.get();
    }
}
