package com.cefalo.cci.restResource;

import com.cefalo.cci.utils.locator.ResourceLocator;
import com.cefalo.cci.model.Content;
import com.cefalo.cci.model.MatchingRules;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.service.MatchingService;
import com.cefalo.cci.service.PublicationService;
import com.cefalo.cci.utils.StringUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.Responses;
import com.sun.jersey.api.view.Viewable;

import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.ResponseHeader;
import org.codehaus.enunciate.jaxrs.ResponseHeaders;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cefalo.cci.utils.StringUtils.isBlank;

/**
 * This class provides a resource that finds the epub version that best matches the characteristics of a device.
 */
@Path("/{organization}/{publication}/matchedContent")
public class ContentResource {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private MatchingService matchingService;

    @Inject
    private PublicationService publicationService;

    @Context
    private Request request;

    @Inject
    @Named("webservice")
    private Provider<ResourceLocator> webserviceLocatorProvider;

    /**
     * Finds the epub versions of issues that best matches a specific device. The characteristics of the device are
     * supplied as parameters to the method.
     * <p>
     * Example query:
     * <p>
     * Example response representation
     *
     * @param organizationName
     *            The identifier of the organization
     * @param publicationName
     *            The identifier of the publication
     * @param width
     *            The screen width of the device
     * @param height
     *            The screen height of the device
     * @param os
     *            The operating system of the device
     * @param osv
     *            The operating system version of the device
     * @param device
     *            The device type
     * @param readerVersion
     *            The version of the reader app installed on the device
     * @param authorization
     *            User name and password, bas64 encoded
     * @return A form that can be used to find issues with the best matching epub type for this particular device.
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
    @GET
    @Produces(MediaType.APPLICATION_XHTML_XML)
    public Response getMatchedContent(
            @PathParam("organization") @DefaultValue("") final String organizationName,
            @PathParam("publication") @DefaultValue("") final String publicationName,
            @QueryParam("width") @DefaultValue("0") final int width,
            @QueryParam("height") @DefaultValue("0") final int height,
            @QueryParam("os") @DefaultValue("") final String os,
            @QueryParam("osv") @DefaultValue("") final String osv,
            @QueryParam("device") @DefaultValue("") final String device,
            @QueryParam("readerVersion") @DefaultValue("") final String readerVersion,
            @HeaderParam("Authorization") String authorization) {

        if (isBlank(publicationName) || isBlank(organizationName)) {
            return Responses.clientError().entity("Organization or publication name may not be empty.").build();
        }

        if (isBlank(device) && width <= 0 && height <= 0 && isBlank(os) && isBlank(osv) && isBlank(readerVersion)) {
            return Responses.clientError().entity("At least one search attribute must be provided.").build();
        }

        Publication publication = publicationService.getPublication(publicationName);
        if (publication == null || !Objects.equal(publication.getOrganization().getId(), organizationName)) {
            throw new NotFoundException("Publication or Organization not found.");
        }

        Content content = new Content(width, height, os, osv, device, readerVersion);
        List<MatchingRules> matchingRules = matchingService.applyMatchingAlgorithm(content, publication);

        if (matchingRules.size() == 0) {
            logger.warn("No matching rule found for {}", content);

            throw new NotFoundException("Epub File For this device is not found");
        } else {
            if (logger.isDebugEnabled()) {
                List<Long> matchedIds = new ArrayList<>();
                for (MatchingRules rule : matchingRules) {
                    matchedIds.add(rule.getId());
                }

                logger.debug("Matched rule IDs for {}: {}", content, Joiner.on(", ").join(matchedIds));
            }
        }

        // Even if we found multiple match, we'll just use the first one, which has the highest priority.
        MatchingRules matchRule = matchingRules.get(0);

        // Check for HTTP 304.
        long lastUpdateTime = matchRule.getUpdated().getTime();
        ResponseBuilder unmodifiedResponseBuilder = request.evaluatePreconditions(EntityTag.valueOf(StringUtils
                .createETagHeaderValue(lastUpdateTime)));
        if (unmodifiedResponseBuilder != null) {
            return unmodifiedResponseBuilder.tag(String.valueOf(lastUpdateTime)).build();
        }

        Map<String, Object> model = new HashMap<>();

        model.put("issueSearchURI", webserviceLocatorProvider.get().getIssueListURI(organizationName, publicationName));
        model.put("epubName", matchRule.getDesignToEpubMapper().getEpubName());

        return Response.ok(new Viewable("/deviceSpecificIssueSearch", model)).build();
    }
}
