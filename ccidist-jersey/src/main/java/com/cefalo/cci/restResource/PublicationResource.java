package com.cefalo.cci.restResource;

import com.cefalo.cci.dao.DriverDao;
import com.cefalo.cci.utils.locator.ResourceLocator;
import com.cefalo.cci.model.DesignToEpubMapper;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.service.PublicationService;
import com.cefalo.cci.utils.StringUtils;
import com.google.common.base.Objects;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cefalo.cci.utils.StringUtils.isBlank;

/**
 * This class is used to obtain information about publications. It produces only one type of
 * representation: The organization detail. The publication detail  contains links to a number of resources related to
 * the publication:
 * <ul>
 *     <li>An HTML form that allows arbitrary search for issues of the publication.
 *     <li>An HTML form to get an <em>access token</em> for a single issue of the publication.
 *     <li>An HTML form to find the variants of issues that best matches a specific device type.
 *     <li>A Link to the URL used to ingest new issues into the publication, and to update existing issues.
 * </ul>
 * Note that the latter three resources are located under the <b>admin</b> part of the web service. These resources
 * require authentication (HTTP basic authentication).
 */
@Path("/{organization}/{publication}/")
public class PublicationResource {
    @Context
    private Request request;

    @Context
    private UriInfo uriInfo;

    @Inject
    private PublicationService publicationService;

    @Inject
    private DriverDao driverDao;

    @Inject
    @Named("webservice")
    private Provider<ResourceLocator> webserviceLocatorProvider;

    @Inject
    @Named("admin")
    private Provider<ResourceLocator> adminLocatorProvider;

    @Inject
    @Named("externalAdmin")
    private Provider<ResourceLocator> externalAdminLocatorProvider;

    // NOTE: Use @DefaulValue. That makes us immune to NULL de-referencing issues.
    // NOTE: Prefer final parameters.
    // NOTE: Jersey can work with primitive types. So method parameters can be "int" or "long".
    // NOTE: If multiple resource methods are used, consider moving the params to member variables.
    /**
     * Gets a publication resource. The representation is described above.
     *
     * <br/><br/>
     * Response body example:
     *
     * <pre class="prettyprint">
     * &nbsp&lt!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd"&gt
     * &nbsp&lthtml xmlns="http://www.w3.org/1999/xhtml" xml:lang="en"&gt
     * &nbsp  &lthead&gt
     * &nbsp  &lttitle&gtPublication&lt/title&gt
     * &nbsp &lt/head&gt
     * &nbsp  &ltbody&gt
     * &nbsp    &lth1&gtPublication&lt/h1&gt
     * &nbsp    &lth3&gt
     * &nbsp    Addressa
     * &nbsp  &lt/h3&gt
     * &nbsp    &ltdiv&gt
     * &nbsp      &lth2&gtIssue Search&lt/h2&gt
     * &nbsp      &ltform action="http://localhost:8080/webservice/polaris/addressa/issues/" enctype="text/plain"&gt
     * &nbsp        &ltdl&gt
     * &nbsp          &ltdt&gt
     * &nbsp            &ltlabel for="limit"&gtLimit&lt/label&gt
     * &nbsp          &lt/dt&gt
     * &nbsp          &ltdd&gt
     * &nbsp            &ltinput type="text" name="limit" id="limit" /&gt
     * &nbsp          &lt/dd&gt
     * &nbsp        &lt/dl&gt
     * &nbsp        &ltdl&gt
     * &nbsp          &ltdt&gt
     * &nbsp            &ltlabel for="start"&gtStart&lt/label&gt
     * &nbsp          &lt/dt&gt
     * &nbsp          &ltdd&gt
     * &nbsp            &ltinput type="text" name="start" id="start" /&gt
     * &nbsp          &lt/dd&gt
     * &nbsp        &lt/dl&gt
     * &nbsp        &ltdl&gt
     * &nbsp          &ltdt&gt
     * &nbsp            &ltlabel for="epubName"&gtEpub Name&lt/label&gt
     * &nbsp          &lt/dt&gt
     * &nbsp          &ltdd&gt
     * &nbsp            &lt;select name="epubName" id="epubName"&gt;
     * &nbsp                &lt;option value="ipad2"&gt;ipad2&lt;/option&gt;
     * &nbsp                &lt;option value="ipad3"&gt;ipad3&lt;/option&gt;
     * &nbsp                &lt;option value="ipad-mini"&gt;ipad-mini&lt;/option&gt;
     * &nbsp            &lt;/select&gt;
     * &nbsp          &lt/dd&gt
     * &nbsp        &lt/dl&gt
     * &nbsp        &ltdl&gt
     * &nbsp          &ltdt&gt
     * &nbsp            &ltlabel for="date"&gtTo date&lt/label&gt
     * &nbsp          &lt/dt&gt
     * &nbsp          &ltdd&gt
     * &nbsp            &ltinput type="text" name="toDate" id="date" /&gtyyyy-MM-dd(Ex.2009-01-01) or yyyy-MM-ddZ(Ex.2009-01-01+0600)
     * &nbsp
     * &nbsp          &lt/dd&gt
     * &nbsp        &lt/dl&gt
     * &nbsp        &ltdl&gt
     * &nbsp          &ltdt&gt
     * &nbsp            &ltlabel for="sortOrder"&gtSort order&lt/label&gt
     * &nbsp          &lt/dt&gt
     * &nbsp          &ltdd&gt
     * &nbsp            &ltselect id="sortOrder" name="sortOrder"&gt
     * &nbsp              &ltoption value="desc"&gtDesc&lt/option&gt
     * &nbsp              &ltoption value="asc"&gtAsc&lt/option&gt
     * &nbsp            &lt/select&gt
     * &nbsp          &lt/dd&gt
     * &nbsp        &lt/dl&gt
     * &nbsp        &ltdl&gt
     * &nbsp          &ltdt&gt
     * &nbsp            &ltlabel for="draft"&gtDraft&lt/label&gt
     * &nbsp          &lt/dt&gt
     * &nbsp          &ltdd&gt
     * &nbsp            &lt;input type="checkbox" id="draft" name="draft" value="true" /&gt;
     * &nbsp          &lt/dd&gt
     * &nbsp        &lt/dl&gt
     * &nbsp        &ltdl&gt
     * &nbsp          &ltdd&gt
     * &nbsp            &ltinput type="submit" name="submit" /&gt
     * &nbsp          &lt/dd&gt
     * &nbsp        &lt/dl&gt
     * &nbsp      &lt/form&gt
     * &nbsp    &lt/div&gt
     * &nbsp    &ltdiv&gt
     * &nbsp      &lth2&gtGenerate access token&lt/h2&gt
     * &nbsp      &ltform action="http://localhost:8080/admin/polaris/addressa/accesstoken" method="post"&gt
     * &nbsp        &ltdl&gt
     * &nbsp          &ltdt&gt
     * &nbsp            &ltlabel for="issueId"&gtIssue id&lt/label&gt
     * &nbsp          &lt/dt&gt
     * &nbsp          &ltdd&gt
     * &nbsp            &ltinput type="text" name="issueId" id="issueId" /&gt
     * &nbsp          &lt/dd&gt
     * &nbsp        &lt/dl&gt
     * &nbsp        &ltdl&gt
     * &nbsp          &ltdd&gt
     * &nbsp            &ltinput type="submit" name="submit" /&gt
     * &nbsp          &lt/dd&gt
     * &nbsp        &lt/dl&gt
     * &nbsp      &lt/form&gt
     * &nbsp    &lt/div&gt
     * &nbsp    &ltdiv&gt
     * &nbsp      &lth2&gtContent matching&lt/h2&gt
     * &nbsp      &ltform action="http://localhost:8080/admin/polaris/addressa/matchedContent"&gt
     * &nbsp        &ltdl&gt
     * &nbsp          &ltdt&gt
     * &nbsp            &ltlabel for="device"&gtDevice Name&lt/label&gt
     * &nbsp          &lt/dt&gt
     * &nbsp          &ltdd&gt
     * &nbsp            &ltinput type="text" name="device" id="device" /&gt
     * &nbsp          &lt/dd&gt
     * &nbsp        &lt/dl&gt
     * &nbsp        &ltdl&gt
     * &nbsp          &ltdt&gt
     * &nbsp            &ltlabel for="width"&gtDevice Width&lt/label&gt
     * &nbsp          &lt/dt&gt
     * &nbsp          &ltdd&gt
     * &nbsp            &ltinput type="text" name="width" id="width" /&gt
     * &nbsp          &lt/dd&gt
     * &nbsp        &lt/dl&gt
     * &nbsp        &ltdl&gt
     * &nbsp          &ltdt&gt
     * &nbsp            &ltlabel for="height"&gtDevice Height&lt/label&gt
     * &nbsp          &lt/dt&gt
     * &nbsp          &ltdd&gt
     * &nbsp            &ltinput type="text" name="height" id="height" /&gt
     * &nbsp          &lt/dd&gt
     * &nbsp        &lt/dl&gt
     * &nbsp        &ltdl&gt
     * &nbsp          &ltdt&gt
     * &nbsp            &ltlabel for="os"&gtOS&lt/label&gt
     * &nbsp          &lt/dt&gt
     * &nbsp          &ltdd&gt
     * &nbsp            &ltinput type="text" name="os" id="os" /&gt
     * &nbsp          &lt/dd&gt
     * &nbsp        &lt/dl&gt
     * &nbsp        &ltdl&gt
     * &nbsp          &ltdt&gt
     * &nbsp            &ltlabel for="osv"&gtOS Version&lt/label&gt
     * &nbsp          &lt/dt&gt
     * &nbsp          &ltdd&gt
     * &nbsp            &ltinput type="text" name="osv" id="osv" /&gt
     * &nbsp          &lt/dd&gt
     * &nbsp        &lt/dl&gt
     * &nbsp        &ltdl&gt
     * &nbsp          &ltdt&gt
     * &nbsp            &ltlabel for="readerVersion"&gtReader Version&lt/label&gt
     * &nbsp          &lt/dt&gt
     * &nbsp          &ltdd&gt
     * &nbsp            &ltinput type="text" name="readerVersion" id="readerVersion" /&gt
     * &nbsp          &lt/dd&gt
     * &nbsp        &lt/dl&gt
     * &nbsp        &ltdl&gt
     * &nbsp          &ltdd&gt
     * &nbsp            &ltinput type="submit" name="submit" /&gt
     * &nbsp          &lt/dd&gt
     * &nbsp        &lt/dl&gt
     * &nbsp      &lt/form&gt
     * &nbsp    &lt/div&gt
     * &nbsp    &ltdiv&gt
     * &nbsp      &ltdl&gt
     * &nbsp        &ltdt&gtuploadcciobjectxml&lt/dt&gt
     * &nbsp        &ltdd&gt
     * &nbsp            &lt;a href="http://localhost:8080/admin/edit/polaris/addressa/issues/cciobjectxml/"&gt;uploadcciobjectxml&lt;/a&gt;
     * &nbsp        &lt/dd&gt
     * &nbsp      &lt/dl&gt
     * &nbsp    &lt/div&gt
     * &nbsp  &lt/body&gt
     * &nbsp&lt/html&gt
     * </pre>
     *
     * @param organizationName The identifier of the organization
     * @param publicationName  The identifier of the publication
     * @param ifNoneMatch Return a representation only if it's Etag differs from this Etag. Otherwise return a 304 response
     * @param ifModifiedSince Unsupported?
     * @return A publication representation.
     */
    @StatusCodes({
            @ResponseCode( code=200, condition="OK" ),
            @ResponseCode( code=304, condition="Not Modified. A matching Etag was provided in the If-None-Match request header")
    })
    @ResponseHeaders({
            @ResponseHeader( name="Etag", description="The Etag stays the same as long as the response representation does not change. " +
                                  "In this case the representation will rarely change as it mostly contains static forms. " +
                                  "Currently it will change if the name of the publication changes."),
            @ResponseHeader( name="Last-Modified", description="The last modified date for the representation."),
            @ResponseHeader( name="Cache-Control", description="Currently unsupported" )
    })
    @GET
    @Produces(MediaType.APPLICATION_XHTML_XML)
    public Response getPublicationDetail(
            @PathParam("organization") @DefaultValue("") final String organizationName,
            @PathParam("publication") @DefaultValue("") final String publicationName,
            @HeaderParam("If-None-Match") String ifNoneMatch,
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {

        // Fail fast. No point in proceeding if our arguments are obviously wrong.
        if (isBlank(publicationName) || isBlank(organizationName)) {
            return Responses.clientError().entity("Organization or publication name may not be empty.").build();
        }

        Publication publication = publicationService.getPublication(publicationName);
        // We should not allow people to trick us, the requested URI should be accurate.
        // TODO: There is a slight performance hit for this. Caching may solve this.
        if (publication == null || !Objects.equal(publication.getOrganization().getId(), organizationName)) {
            throw new NotFoundException();
        }

        Date lastUpdateTime = publication.getUpdated();
        // Support conditional GET requests
        ResponseBuilder unmodifiedResponseBuilder = request.evaluatePreconditions(lastUpdateTime,
                EntityTag.valueOf(StringUtils
                        .createETagHeaderValue(publication.getVersion())));
        if (unmodifiedResponseBuilder != null) {
            return unmodifiedResponseBuilder.tag(String.valueOf(publication.getVersion())).lastModified(lastUpdateTime).build();
        }


        // FIXME: It would be hard to test this :-(. One option is to create a base class for all resources and return
        // the locator from a method that we can override for testing. Best would be if we can inject this via Guice.

        List<DesignToEpubMapper> designToEpubMapperList = driverDao.getAllDesignToEpubMapperByPublicationId(publication.getId());
        ResourceLocator webserviceLocator = webserviceLocatorProvider.get();
        ResourceLocator adminLocator = adminLocatorProvider.get();
        ResourceLocator externalAdminLocator = externalAdminLocatorProvider.get();

        // NOTE: Use JDK 7 diamond operator :-)
        Map<String, Object> model = new HashMap<>();
        model.put("publication", publication);

        // All URIs must come from the resource locators. Otherwise, we'll have a hard time to maintain this.
        model.put("issueSearchURI", webserviceLocator.getIssueListURI(organizationName, publicationName));

        model.put("tokenURI", adminLocator.getTokenURI(organizationName, publicationName));
        model.put("matchedContentURI", adminLocator.getMatchedContentURI(organizationName, publicationName));

        model.put("cciObjectXMLUploadURI",
                externalAdminLocator.getCCIObjectXMLUploadURI(organizationName, publicationName));

        model.put("designToEpubMapperList", designToEpubMapperList);

        ResponseBuilder responseBuilder = Response.ok(new Viewable("/publication", model));
        // We should add the version string in the ETag header.
        responseBuilder = responseBuilder.tag(String.valueOf(publication.getVersion())).lastModified(lastUpdateTime);

        return responseBuilder.build();
    }
}
