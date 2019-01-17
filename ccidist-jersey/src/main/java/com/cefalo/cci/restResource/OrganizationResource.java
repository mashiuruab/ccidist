package com.cefalo.cci.restResource;

import com.cefalo.cci.utils.locator.ResourceLocator;
import com.cefalo.cci.model.Organization;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.service.OrganizationService;
import com.cefalo.cci.utils.StringUtils;
import com.google.inject.Inject;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.Responses;
import com.sun.jersey.api.view.Viewable;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.ResponseHeader;
import org.codehaus.enunciate.jaxrs.ResponseHeaders;
import org.codehaus.enunciate.jaxrs.StatusCodes;

import javax.inject.Named;
import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to get information about organizations. Two types representations can be
 * obtained from this resource:
 * <ul>
 *     <li>The list of all organizations. THis is the root representation of the public web service.</li>
 *     <li>The organization details; one representation for each organization.</li>
 * </ul>
 * The latter representations are all hyperlinked from the organization list.
 */
@Path("/")
public class OrganizationResource {
    @Inject
    private OrganizationService organizationService;

    @Context
    private UriInfo uriInfo;

    @Context
    private Request request;

    @Inject
    @Named("webservice")
    private Provider<ResourceLocator> webserviceLocatorProvider;

    /**
     * Gets the organization list. This is the root representation of the REST web service. It contains
     * a list of hyperlinks to the individual organization representations.
     *
     * <br/><br/>
     * Response body example:
     *
     * <br/>
     * <pre class="prettyprint">
     * &lt!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd"&gt
     * &lthtml xmlns="http://www.w3.org/1999/xhtml" xml:lang="en"&gt
     * &lthead&gt
     * &nbsp &lttitle&gtOrganization List&lt/title&gt
     * &lt/head&gt
     * &ltbody&gt
     * &nbsp  &lth1&gtOrganization List&lt/h1&gt
     * &nbsp  &ltul class="organizations"&gt
     * &nbsp    &ltli&gt
     * &nbsp      &lta href="http://ccieurope.com/webservice/axelspringer"&gtAxelSpringer&lt/a&gt
     * &nbsp    &lt/li&gt
     * &nbsp    &ltli&gt
     * &nbsp      &lta href="http://ccieurope.com/webservice/News"&gtNews Corp&lt/a&gt
     * &nbsp    &lt/li&gt
     * &nbsp    &ltli&gt
     * &nbsp      &lta href="http://ccieurope.com/webservice/nhst"&gtNHST&lt/a&gt
     * &nbsp    &lt/li&gt
     * &nbsp    &ltli&gt
     * &nbsp      &lta href="http://ccieurope.com/webservice/polaris"&gtPolaris&lt/a&gt
     * &nbsp    &lt/li&gt
     * &nbsp &lt/ul&gt
     * &lt/body&gt
     * &lt/html&gt
     * </pre>
     *
     * @return A representation containing the list of organizations. This is the "root" representation of the
     *         webservice
     *
     * @param ifNoneMatch Return a representation only if it's Etag differs from this Etag. Otherwise return a 304 response
     *                    code
     */
    @GET
    @Produces(MediaType.APPLICATION_XHTML_XML)
    @StatusCodes({
            @ResponseCode( code=200, condition="OK" ),
            @ResponseCode( code=304, condition="Not Modified. A matching Etag was provided in the If-None-Match request header")
    })
    @ResponseHeaders({
            @ResponseHeader( name="Etag", description="The Etag stays the same as long as the response representation does not change"),
            @ResponseHeader( name="Last-Modified", description="Currently unsupported"),
            @ResponseHeader( name="Cache-Control", description="Currently unsupported" )
    })
    public Response getOrganizationList(@HeaderParam("If-None-Match") String ifNoneMatch) {
        List<Organization> organizations = organizationService.getAllOrganizations();
        if (organizations.isEmpty()) {
            throw new NotFoundException("No organization in the system.");
        }

        long totalUpdatedTime = 0;
        for (Organization org : organizations) {
               totalUpdatedTime += org.getUpdated().getTime();
        }
        ResponseBuilder unmodifiedResponseBuilder = request.evaluatePreconditions(EntityTag.valueOf(StringUtils.createETagHeaderValue(totalUpdatedTime)));
        if (unmodifiedResponseBuilder != null) {
            return unmodifiedResponseBuilder.tag(String.valueOf(totalUpdatedTime)).build();
        }

        Map<Organization, URI> orgNameUriMap = new LinkedHashMap<>();

        for (Organization organization : organizations) {
            orgNameUriMap.put(organization, webserviceLocatorProvider.get().getOrganizationURI(organization.getId()));
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("orgMap", orgNameUriMap);

        //TODO: we have to add version here

        return Response.ok(new Viewable("/organizationList", model)).tag(String.valueOf(totalUpdatedTime)).build();
    }

    /**
     * Gets an organization representation. The representation contains a list of hyperlinked the individual
     * publications owned by the organization.
     *
     * <br/><br/>
     * Response body example:
     *
     * <pre class="prettyprint">
     * &lt!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd"&gt
     * &lthtml xmlns="http://www.w3.org/1999/xhtml" xml:lang="en"&gt
     * &lthead&gt
     * &nbsp &lttitle&gtOrganization&lt/title&gt
     * &lt/head&gt
     * &ltbody&gt
     * &nbsp  &lth1&gtOrganization&lt/h1&gt
     * &nbsp  &lth3&gtPolaris&lt/h3&gt
     * &nbsp  &ltul class="organization"&gt
     * &nbsp    &ltli&gt
     * &nbsp      &lta href="http://ccieurope.com/webservice/polaris/addressa"&gtAddressa&lt/a&gt
     * &nbsp    &lt/li&gt
     * &nbsp    &ltli&gt
     * &nbsp      &lta href="http://ccieurope.com/webservice/polaris/harstadtidende"&gtHarstadtidende&lt/a&gt
     * &nbsp    &lt/li&gt
     * &nbsp  &lt/ul&gt
     * &lt/body&gt
     * &lt/html&gt
     * </pre>
     *
     * @param organizationId The identifier of the organization.
     * @param ifNoneMatch Return a representation only if it's Etag differs from this Etag. Otherwise return a 304 response
     *                    code
     * @return A representation of the  organization.
     */
    @StatusCodes({
            @ResponseCode( code=200, condition="OK" ),
            @ResponseCode( code=304, condition="Not Modified. A matching Etag was provided in the If-None-Match request header")
    })
    @ResponseHeaders({
            @ResponseHeader( name="Etag", description="The Etag stays the same as long as the response representation does not change"),
            @ResponseHeader( name="Last-Modified", description="Currently unsupported"),
            @ResponseHeader( name="Cache-Control", description="Currently unsupported" )
    })
    @GET
    @Path("/{organization}/")
    @Produces(MediaType.APPLICATION_XHTML_XML)
    public Response getOrganizationDetail(
            @PathParam("organization") final String organizationId,
            @HeaderParam("If-None-Match") String ifNoneMatch) {
        if (StringUtils.isBlank(organizationId)) {
            return Responses.clientError().entity("Organization name may not be empty").build();
        }

        Organization organization = organizationService.getOrganization(organizationId);
        if (organization == null) {
            throw new NotFoundException(String.format("No organization '%s' found.", organizationId));
        }

        long totalUpdatedTime = organization.getUpdated().getTime();
        // FIXME: Organization#getPublications is not cached. So, this goes to the DB all the time.
        if (organization.getPublications() != null) {
            for (Publication publication : organization.getPublications()) {
                totalUpdatedTime += publication.getUpdated().getTime();
            }
        }

        ResponseBuilder unmodifiedResponseBuilder = request.evaluatePreconditions(EntityTag.valueOf(StringUtils.createETagHeaderValue(totalUpdatedTime)));
        if (unmodifiedResponseBuilder != null) {
            return unmodifiedResponseBuilder.tag(String.valueOf(totalUpdatedTime)).build();
        }

        Map<Publication, URI> publicationNameUriMap = new LinkedHashMap<>();

        for (Publication publication : organization.getPublications()) {
            publicationNameUriMap.put(publication,
                    webserviceLocatorProvider.get().getPublicationURI(organization.getId(), publication.getId()));
        }

        Map<String, Object> model = new HashMap<>();
        model.put("organizationName", organization.getName());
        model.put("publicationMap", publicationNameUriMap);

        return Response.ok(new Viewable("/organization", model)).tag(String.valueOf(totalUpdatedTime)).build();
    }
}
