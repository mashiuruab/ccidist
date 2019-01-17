package com.cefalo.cci.restResource;

import static com.cefalo.cci.utils.DateUtils.convertTimeStampWithTZ;
import static com.cefalo.cci.utils.StringUtils.isBlank;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import javax.inject.Provider;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.ResponseHeader;
import org.codehaus.enunciate.jaxrs.ResponseHeaders;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.utils.locator.ResourceLocator;
import com.cefalo.cci.model.Issue;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.model.token.AccessToken;
import com.cefalo.cci.model.token.ProductID;
import com.cefalo.cci.service.IssueService;
import com.cefalo.cci.service.PublicationService;
import com.cefalo.cci.service.TokenGenerator;
import com.cefalo.cci.service.UsersService;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.Responses;

/**
 * This class provides a resource that will create access tokens. Access tokens can be provided for individual issues
 * and for publications. A publication token allows access to all issues of the publication.
 */
@Path("/{organization}/{publication}/accesstoken")
public class AccessTokenResource {
    private final Logger logger = LoggerFactory.getLogger(AccessTokenResource.class);

    @Inject
    private TokenGenerator tokenGenerator;

    @Inject
    private ApplicationConfiguration config;

    @Inject
    private PublicationService publicationService;

    @Inject
    private IssueService issueService;

    @Inject
    private UsersService usersService;

    @Context
    UriInfo uriInfo;

    @Inject
    @Named("webservice")
    private Provider<ResourceLocator> webServiceLocatorProvider;

    /**
     * Creates a new access token. If an issueId is provided, the token will grant access to that single issue. If no
     * issueId is provided, the token will grant access to all issues of the publication. The token expiry time is a
     * fixed time interval, configurable on the server <br>
     * This method requires user name and password (basic authentication).
     *
     * @param organizationId
     *            The identifier of the organization
     * @param publicationId
     *            The identifier of the publication
     * @param issueLink
     *            Optional issue link. This is the URL in the &lt;link&gt; element of the issue list atom feed entries.
     * @param authorization
     *            User name and password, bas64 encoded
     * @return A cryptographically signed token
     * @throws MalformedURLException
     *             If the issueLink param is not a valid {@link URL}
     */
    @StatusCodes({
            @ResponseCode(code = 200, condition = "OK"),
            @ResponseCode(code = 400, condition = "Bad Request."),
            @ResponseCode(code = 401, condition = "Unauthorized. User name and password missing or wrong.")
    })
    @ResponseHeaders({
            @ResponseHeader(name = "Cache-Control", description = "Currently unsupported"),
            @ResponseHeader(name = "WWW-Authenticate", description = "Identifies authentication method and realm.")
    })
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response getAccessToken(
            @PathParam("organization") @DefaultValue("") final String organizationId,
            @PathParam("publication") @DefaultValue("") final String publicationId,
            @FormParam("issueLink") @DefaultValue("") final String issueLink,
            @HeaderParam("Authorization") String authorization) throws MalformedURLException {
        if (isBlank(publicationId) || isBlank(organizationId)) {
            return Responses.clientError().entity("Organization or publication name may not be blank.").build();
        }

        Publication publication = publicationService.getPublication(publicationId);
        if (publication == null || !Objects.equal(publication.getOrganization().getId(), organizationId)) {
            throw new NotFoundException();
        }

        // FIXME: I don't think we should check role like this. The "security" module should handle all this.
        if (!usersService.hasRole(publicationId, Arrays.asList("Portal", "Ingester"))) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        ProductID productId;
        Long issueID = null;
        if (isBlank(issueLink)) {
            productId = new ProductID(publicationId);
        } else {
            try {
                issueID = getIssueId(issueLink, organizationId, publicationId);
                productId = new ProductID(publicationId, issueID);

                Issue issue = issueService.getIssue(productId.getIssueID());
                if (issue == null) {
                    throw new WebApplicationException(
                            Responses
                                    .clientError()
                                    .entity(String.format("Issue %s does not exist.", issueID))
                                    .build());
                } else if (!issue.getPublication().getId().equals(publicationId)) {
                    throw new WebApplicationException(
                            Responses
                                    .clientError()
                                    .entity("Issue publication is wrong.")
                                    .build());
                }
            } catch (NumberFormatException nfe) {
                logger.error("Error while trying to figure out issue id.", nfe);
                throw new WebApplicationException(Responses.clientError().entity("Malformed issue link").build());
            }
        }

        long timestamp = MILLISECONDS.toSeconds(
                convertTimeStampWithTZ(System.currentTimeMillis() + config.getTokenValidityDuration()));

        AccessToken accessToken = tokenGenerator.generateAccessToken(timestamp, productId);
        return Response.ok(accessToken.toString()).build();
    }

    private Long getIssueId(String issueUri, String organizationId, String publicationId) throws NumberFormatException {
        String dummyIssueUri = webServiceLocatorProvider.get().getIssueURI(organizationId, publicationId, -1)
                .toString();
        String commonPrefix = Strings.commonPrefix(dummyIssueUri, issueUri);

        String epubContentPath = issueUri.substring(commonPrefix.length());
        String pathInside = epubContentPath.replaceAll("^\\d+", "");
        String issueId = epubContentPath.replace(pathInside, "");

        // The part after the common prefix is the issue ID and it must be a number. So, we won't do more checking here.
        // Instead, we'll just try to create the number.
        Long issueID = Long.valueOf(issueId);
        if (issueID <= 0) {
            throw new IllegalArgumentException("Issue ID must be a positive number. Value: " + issueId);
        }
        return issueID;

    }
}
