package com.cefalo.cci.restResource;

import static com.cefalo.cci.utils.StringUtils.isBlank;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Provider;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.ResponseHeader;
import org.codehaus.enunciate.jaxrs.ResponseHeaders;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.config.MimeTypeConfiguration;
import com.cefalo.cci.dao.DriverDao;
import com.cefalo.cci.enums.IssueStatus;
import com.cefalo.cci.enums.SortBy;
import com.cefalo.cci.utils.locator.ResourceLocator;
import com.cefalo.cci.model.Issue;
import com.cefalo.cci.model.Organization;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.model.SectionImage;
import com.cefalo.cci.model.token.ProductID;
import com.cefalo.cci.service.ChangelogService;
import com.cefalo.cci.service.IssueService;
import com.cefalo.cci.service.PublicationService;
import com.cefalo.cci.storage.Storage;
import com.cefalo.cci.utils.DateUtils;
import com.cefalo.cci.utils.StringUtils;
import com.google.common.base.Stopwatch;
import com.google.common.io.Closeables;
import com.google.inject.Inject;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.Responses;
import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.core.header.reader.HttpHeaderReader;
import com.sun.syndication.feed.synd.SyndFeed;

/**
 * This class is used to obtain information about individual newspaper issues. It supports a number of search query
 * resources, and delivers a number of different representations as responses to those queries.
 */
@net.bull.javamelody.MonitoredWithGuice(name = "issueStat")
@Path("/{organization}/{publication}/issues/")
public class IssueResource {
    private static final String ACCESS_TOKEN_PARAM = "accesstoken";
    private static final String DATE_FORMAT_TZ = "yyyy-MM-ddZ";

    private final Logger logger = LoggerFactory.getLogger(IssueResource.class);

    @Inject
    private IssueService issueService;

    @Inject
    private Storage<Issue> epubStorage;

    @Inject
    private PublicationService publicationService;

    @Inject
    private ChangelogService eventService;

    @Inject
    private DriverDao driverDao;

    @Context
    private Request request;

    @Context
    private UriInfo uriInfo;

    @Inject
    private ApplicationConfiguration applicationConfiguration;

    @Inject
    private MimeTypeConfiguration mimeTypeConfiguration;

    @Inject
    @Named("webservice")
    private Provider<ResourceLocator> webserviceLocatorProvider;

    /**
     * Gets a list of issues available for a publication. The resource returned is a paged list of issues ordered by
     * issue date, in the form of an <a href="http://en.wikipedia.org/wiki/Atom_(standard)">Atom</a> feed. All
     * parameters are optional, except organizationName, publicationName, and epubName. <br>
     * Query examples:
     * <ul>
     * <li><em>http://localhost:8080/webservice/polaris/addressa/issues/?epubName=ipad3</em> - get the ipad3 version of
     * the latest issue of the publication "Adressa".
     * </ul>
     *
     * Response body example:
     *
     * <pre class="prettyprint">
     * &lt?xml version="1.0" encoding="UTF-8"?&gt
     * &ltfeed xmlns="http://www.w3.org/2005/Atom" xmlns:dc="http://purl.org/dc/elements/1.1/"&gt
     * &nbsp	&lttitle&gtAddressa issues&lt/title&gt
     * &nbsp    &ltlink rel="self" href="http://ccieurope.com/webservice/polaris/addressa/issues/?start=1* &amp;limit=1* &amp;draft=false* &amp;epubName=ipad3* &amp;sortBy=created* &amp;sortOrder=desc" /&gt
     * &nbsp    &ltauthor&gt
     * &nbsp        &ltname&gtAddressa&lt/name&gt
     * &nbsp    &lt/author&gt
     * &nbsp    &ltid&gthttp://localhost:8080/cciService/Polaris/Addressa/issues/&lt/id&gt
     * &nbsp    &ltupdated&gt2013-06-05T07:07:04Z&lt/updated&gt
     * &nbsp    &ltdc:date&gt2013-06-05T07:07:04Z&lt/dc:date&gt
     * &nbsp    &ltentry&gt
     * &nbsp        &lttitle&gt01-24-2013&lt/title&gt
     * &nbsp        &ltlink rel="alternate" href="http://ccieurope.com/webservice/polaris/addressa/issues/2" /&gt
     * &nbsp        &ltlink rel="public" href="http://localhost:8080/webservice/polaris/addressa/issues/2/public" /&gt
     * &nbsp        &ltauthor&gt
     * &nbsp            &ltname&gtAddressa&lt/name&gt
     * &nbsp        &lt/author&gt
     * &nbsp        &ltid&gturn:uuid:2&lt/id&gt
     * &nbsp        &ltupdated&gt2013-06-05T07:06:48Z&lt/updated&gt
     * &nbsp        &ltpublished&gt2013-01-23T23:00:00Z&lt/published&gt
     * &nbsp        &ltdc:creator&gtAddressa&lt/dc:creator&gt
     * &nbsp        &ltdc:date&gt2013-01-23T23:00:00Z&lt/dc:date&gt
     * &nbsp    &lt/entry&gt
     * &lt/feed&gt
     * </pre>
     *
     * Note that the each &ltentry&gt in the list contains a link to the actual issue (link with rel="alternate"), and a
     * link to the public part of the issue (link with rel="public"). The full issue can only be accessed with a valid
     * <em>access token</em>. The "public" issue resource needs no access token.
     * <p>
     * If the result list contains multiple pages (number of entries is larger than the "limit" parameter) the
     * representation will contain "next" and "prev" links to navigate to next and previous pages, respectively, as
     * specified in the Atom standard.
     *
     * @param organizationName
     *            The identifier of the organization
     * @param publicationName
     *            The identifier of the publication
     * @param start
     *            The offset of the page
     * @param limit
     *            The maximum number elements per page in the result
     * @param epubName
     *            The name of the device version
     * @param toDate
     *            The cutoff date for issues requested. No value provided is interpreted as "today"
     * @param sortOrder
     *            "asc" (ascending) or "desc" (descending)
     * @param draft
     *            Include draft issues?
     * @param sortBy
     *            Which date field to sort by, "created" (?)
     * @return A paged list of issues. The list is a standard Atom feed, as described above.
     * @throws UnsupportedEncodingException
     */
    @StatusCodes({
            @ResponseCode(code = 200, condition = "OK"),
            @ResponseCode(code = 304, condition = "NOTE: Unsupported for this resource."),
            @ResponseCode(code = 400, condition = "Bad Request. Response body will say what's wrong, - typically missing or invalid parameters"),
            @ResponseCode(code = 404, condition = "Not Found. No issues matched the search criteria.") })
    @ResponseHeaders({ @ResponseHeader(name = "Etag", description = "Currently unsupported."),
            @ResponseHeader(name = "Last-Modified", description = "Currently unsupported."),
            @ResponseHeader(name = "Cache-Control", description = "Defines how long the result list can be cached.") })
    @GET
    @Produces(MediaType.APPLICATION_ATOM_XML + ";charset=UTF-8")
    public Response getIssueList(@PathParam("organization") @DefaultValue("") final String organizationName,
            @PathParam("publication") @DefaultValue("") final String publicationName,
            @QueryParam("start") @DefaultValue("1") final int start,
            @QueryParam("limit") @DefaultValue("1") final int limit,
            @QueryParam("epubName") @DefaultValue("") String epubName,
            @QueryParam("toDate") @DefaultValue("") String toDate,
            @QueryParam("sortOrder") @DefaultValue("desc") final String sortOrder,
            @QueryParam("draft") @DefaultValue("false") final boolean draft,
            @QueryParam("sortBy") @DefaultValue("created") String sortBy) throws UnsupportedEncodingException {
        if (isBlank(publicationName) || isBlank(organizationName)) {
            return Responses.clientError().entity("Organization or publication name may not be blank.").build();
        }

        if (isBlank(epubName)) {
            return Responses.clientError().entity("Epub name can not be blank").build();
        }

        if (start <= 0 || limit <= 0) {
            return Responses.clientError().entity("Start & limit params should have positive non-zero values.").build();
        }

        Date toDateVal = null;
        if (!sortBy.toLowerCase().equals(SortBy.CREATED.getValue())
                && !sortBy.toLowerCase().equals(SortBy.UPDATED.getValue())) {
            return Responses.clientError().entity("sortBy query parameter is not valid").build();
        }

        List<Long> epubNameIds = driverDao.getDesignToEpubMapperIds(publicationName, epubName);
        if (epubNameIds == null || epubNameIds.size() == 0) {
            throw new NotFoundException("Invalid Epub Name");
        }

        if (isBlank(toDate)) {
            Date date = new DateTime().withTimeAtStartOfDay().plusDays(1).toDate();
            toDateVal = DateUtils.convertDateWithTZ(date);
        } else {
            try {
                // toDate = URLEncoder.encode(toDate, "UTF-8");
                if (DateUtils.isDateInDefaultDateFormat(toDate)) {
                    toDateVal = new DateTime(DateUtils.convertDateFormatTZ(toDate))
                            .withTimeAtStartOfDay()
                            .plusDays(1)
                            .toDate();
                } else {
                    toDateVal = new DateTime(DateUtils.convertDateFormatTZ(toDate, DATE_FORMAT_TZ))
                            .withTimeAtStartOfDay()
                            .plusDays(1)
                            .toDate();
                }
            } catch (IllegalArgumentException e) {
                return Responses.clientError().entity("Invalid Date format.").build();
            }
        }

        Publication publication = publicationService.getPublication(publicationName);
        if (publication == null || !Objects.equals(publication.getOrganization().getId(), organizationName)) {
            return Responses.notFound().build();
        }

        SyndFeed feed = issueService.getIssuesAsAtomFeed(
                publication.getOrganization(),
                publication,
                start, limit,
                epubName, epubNameIds,
                toDate, toDateVal,
                sortOrder, sortBy,
                draft);

        // FIXME: Implement ETag & Last-Modified-Date support here. Issue: http://jira.cefalo.com.bd/browse/CCIDIST-117
        // It is rather hard to do this. Since we use paging on this resource and also support sort-order, the last
        // modified date isn't really clear. We need to first decide what should be the last modified date. There is the
        // specifications but on the other hand, we need to find out how it would be useful for the app developer.

        // By default, clients can cache this resource for 5 minutes.
        CacheControl cacheControl = new CacheControl();
        cacheControl.setMaxAge((int) TimeUnit.MINUTES.toSeconds(5));
        cacheControl.setMustRevalidate(false);

        return Response.ok(feed).cacheControl(cacheControl).build();
    }

    /**
     * Gets an issue representation. The issue will be accessible only if a valid <em>access token</em> is provided as
     * part of the request.
     * <p>
     * Example of an issue representation returned in the response body:
     * <p>
     *
     * <pre class="prettyprint">
     * &lt!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd"&gt
     * &lthtml xmlns="http://www.w3.org/1999/xhtml" xml:lang="en"&gt
     * &nbsp &lthead&gt
     * &nbsp   &lttitle&gtAddressa 2 for ipad3&lt/title&gt
     * &nbsp &lt/head&gt
     * &nbsp   &ltbody&gt
     * &nbsp     &ltdl class="issue"&gt
     * &nbsp       &ltdt&gtname&lt/dt&gt &ltdd&gtAddressa&lt/dd&gt
     * &nbsp       &ltdt&gtissue&lt/dt&gt &ltdd&gt2&lt/dd&gt
     * &nbsp       &ltdt&gtdate&lt/dt&gt &ltdd&gt2013-06-11&lt/dd&gt
     * &nbsp       &ltdt&gttitle&lt/dt&gt&ltdd&gtAddressa&lt/dd&gt
     * &nbsp       &ltdt&gtstatus&lt/dt&gt&ltdd&gtPUBLISHED&lt/dd&gt
     * &nbsp       &ltdt&gtcover-image&lt/dt&gt
     * &nbsp         &ltdd&gt
     * &nbsp           &lta href="http://localhost:8080/webservice/polaris/addressa/issues/2/OPS/Edition+G00FBC81.1_bitmap500.png?accesstoken=3be6e0dc0970a22c6c04b3e658a59ea1|1371136753310|addressa,* &nbsp"&gtCover image&lt/a&gt
     * &nbsp         &lt/dd&gt
     * &nbsp       &ltdt&gtepub&lt/dt&gt
     * &nbsp         &ltdd&gt
     * &nbsp           &lta href="http://localhost:8080/webservice/polaris/addressa/issues/2.epub?accesstoken=3be6e0dc0970a22c6c04b3e658a59ea1|1371136753310|addressa,* &nbsp"&gtEpub&lt/a&gt
     * &nbsp       &lt/dd&gt
     * &nbsp       &ltdt&gtcontainer&lt/dt&gt
     * &nbsp         &ltdd&gt
     * &nbsp           &lta href="http://localhost:8080/webservice/polaris/addressa/issues/2/META-INF/container.xml?accesstoken=3be6e0dc0970a22c6c04b3e658a59ea1|1371136753310|addressa,* &nbsp"&gtContainer&lt/a&gt
     * &nbsp         &lt/dd&gt
     * &nbsp       &ltdt&gtevents&lt/dt&gt
     * &nbsp         &ltdd&gt
     * &nbsp           &lta href="http://localhost:8080/webservice/polaris/addressa/issues/2/events?accesstoken=3be6e0dc0970a22c6c04b3e658a59ea1|1371136753310|addressa,* &nbsp"&gtEvents&lt/a&gt
     * &nbsp         &lt/dd&gt
     * &nbsp     &lt/dl&gt
     * &nbsp   &lt/body&gt
     * &lt/html&gt
     * </pre>
     *
     * Explanation follows here.
     *
     * @param organizationId
     *            The identifier of the organization
     * @param publicationId
     *            The identifier of the publication
     * @param issueId
     *            The issue identifier
     * @param accesstoken
     *            An authentication token.
     * @return An issue representation. The issue representation is the starting point for navigating to all the objects
     *         of the issue, as explained in connection with the example above.
     * @throws IOException
     */
    @StatusCodes({
            @ResponseCode(code = 200, condition = "OK"),
            @ResponseCode(code = 304, condition = "NOTE: Unsupported for this resource."),
            @ResponseCode(code = 400, condition = "Bad Request. Response body will say what's wrong, - typically missing or invalid parameters"),
            @ResponseCode(code = 401, condition = "Unauthorized. The authorization token is missing, invalid, or expired.") })
    @ResponseHeaders({ @ResponseHeader(name = "Etag", description = "Currently unsupported."),
            @ResponseHeader(name = "Last-Modified", description = "Currently unsupported."),
            @ResponseHeader(name = "Cache-Control", description = "Defines how long the result list can be cached.") })
    @GET
    @Path("/{issue}/")
    @Produces(MediaType.APPLICATION_XHTML_XML)
    public Response getIssueDetail(
            @PathParam("organization") @DefaultValue("") final String organizationId,
            @PathParam("publication") @DefaultValue("") final String publicationId,
            @PathParam("issue") @DefaultValue("0") final long issueId,
            @QueryParam(ACCESS_TOKEN_PARAM) @DefaultValue("") String accesstoken) throws IOException {

        Issue issue = retrieveIssue(organizationId, publicationId, issueId);
        Publication publication = issue.getPublication();
        Organization organization = publication.getOrganization();

        ResponseBuilder notModifiedResponseBuilder = request.evaluatePreconditions(issue.getUpdated(),
                EntityTag.valueOf(StringUtils.createETagHeaderValue(issue.getVersion())));
        if (notModifiedResponseBuilder != null) {
            return notModifiedResponseBuilder
                    .header(applicationConfiguration.getProductIDHeaderName(),
                            generateProductId(publication.getId(), issue.getId()))
                    .tag(String.valueOf(issue.getVersion())).lastModified(issue.getUpdated()).build();
        }

        // Make sure that we have an up-2-date EPUB
        if (issueService.isEpubOutdated(issue)) {
            issueService.generateEpubIfNecessary(issue);
        }

        String coverImageLink;
        InputStream inputStream = null;
        try {
            coverImageLink = issueService.getCoverImageLink(issue);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cover Image Link parsing error", e);
        } finally {
            Closeables.close(inputStream, false);
        }

        ResourceLocator webServiceResourceLocator = webserviceLocatorProvider.get();
        URI organizationUri = webServiceResourceLocator.getOrganizationURI(organizationId);
        URI publicationUri = webServiceResourceLocator.getPublicationURI(organizationId, publication.getId());

        Map<String, Object> model = new HashMap<>();
        model.put("organization", organization);
        model.put("publication", publication);
        model.put("organizationUri", organizationUri);
        model.put("publicationUri", publicationUri);
        model.put("issue", issue);

        Map<Integer, String> statusMap = new HashMap<>(4);
        for (IssueStatus status : IssueStatus.values()) {
            statusMap.put(status.getValue(), status.toString());
        }
        model.put("statusMap", statusMap);

        ResourceLocator resourceLocator = webserviceLocatorProvider.get();
        if (!isBlank(coverImageLink)) {
            model.put("imageUri",
                    resourceLocator.getEpubContentURI(organizationId, publicationId, issueId, coverImageLink));
        }
        model.put("binaryUri", resourceLocator.getEpubBinaryURI(organizationId, publicationId, issueId));
        model.put("containerUri",
                resourceLocator.getEpubContentURI(
                        organizationId,
                        publicationId,
                        issueId,
                        IssueService.CONTAINER_RELATIVE_PATH));
        model.put("eventsUri", resourceLocator.getEventQueueURI(organizationId, publicationId, issueId));

        return Response
                .ok(new Viewable("/issueDetail", model))
                .header(applicationConfiguration.getProductIDHeaderName(),
                        generateProductId(publication.getId(), issue.getId()))
                .tag(String.valueOf(issue.getVersion())).lastModified(issue.getUpdated()).build();
    }

    /**
     * Gets a representation containing all the "public" resources of an issue. This resource, as well as all resources
     * linked from the representation in the response body, are accessible without a token.
     * <p>
     * Example of a :
     * <p>
     *
     * <pre class="prettyprint">
     * &lt!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd"&gt
     * &lthtml xmlns="http://www.w3.org/1999/xhtml" xml:lang="en"&gt
     * &nbsp  &lthead&gt
     * &nbsp    &lttitle&gtAddressa 2 for ipad3&lt/title&gt
     * &nbsp  &lt/head&gt
     * &nbsp  &ltbody&gt
     * &nbsp    &ltul class="cover-image"&gt
     * &nbsp      &ltli&gt
     * &nbsp        &lta href="http://ccieurope.com/webservice/polaris/addressa/issues/2/OPS/Edition+G00FBC81.1_bitmap500.png"&gthttp://localhost:8080/webservice/polaris/addressa/issues/2/OPS/Edition+G00FBC81.1_bitmap500.png&lt/a&gt
     * &nbsp      &lt/li&gt
     * &nbsp    &lt/ul&gt
     * &nbsp    &ltul class="public"&gt
     * &nbsp      &ltli&gt
     * &nbsp        &lta href="http://ccieurope.com/webservice/polaris/addressa/issues/2/OPS/Edition+G00FBC80.1_bitmap500.png"&gthttp://localhost:8080/webservice/polaris/addressa/issues/2/OPS/Edition+G00FBC80.1_bitmap500.png&lt/a&gt
     * &nbsp      &lt/li&gt
     * &nbsp    &lt/ul&gt
     * &nbsp  &lt/body&gt
     * &lt/html&gt
     * </pre>
     *
     * @param organizationId
     *            The identifier of the organization
     * @param publicationId
     *            The identifier of the publication
     * @param issueId
     *            The identifier of the issue
     * @param ifNoneMatch
     *            Return a representation only if it's Etag differs from this Etag. Otherwise return a 304 response.
     * @param ifModifiedSince
     *            Unsupported?
     * @return
     * @throws IOException
     */
    @StatusCodes({
            @ResponseCode(code = 200, condition = "OK"),
            @ResponseCode(code = 304, condition = "Not Modified. - A matching Etag was provided in the If-None-Match request header."),
            @ResponseCode(code = 400, condition = "Bad Request. - Response body contains error message") })
    @ResponseHeaders({
            @ResponseHeader(name = "Etag", description = "The Etag stays the same as long as the issue is not updated."),
            @ResponseHeader(name = "Last-Modified", description = "Last modified date of issue."),
            @ResponseHeader(name = "Cache-Control", description = "Currently unsupported.") })
    @GET
    @Path("/{issue}/public")
    @Produces(MediaType.APPLICATION_XHTML_XML)
    public Response getPublicIssueDetail(@PathParam("organization") @DefaultValue("") final String organizationId,
            @PathParam("publication") @DefaultValue("") final String publicationId,
            @PathParam("issue") @DefaultValue("0") final long issueId,
            @HeaderParam("If-None-Match") String ifNoneMatch, @HeaderParam("If-Modified-Since") String ifModifiedSince)
            throws IOException {
        Issue issue = retrieveIssue(organizationId, publicationId, issueId);

        Date issueUpdated = issue.getUpdated();
        long issueVersion = issue.getVersion();
        ResponseBuilder notModifiedResponseBuilder = request.evaluatePreconditions(
                issueUpdated,
                EntityTag.valueOf(StringUtils.createETagHeaderValue(issueVersion)));
        if (notModifiedResponseBuilder != null) {
            return notModifiedResponseBuilder
                    .tag(String.valueOf(issueVersion))
                    .lastModified(issueUpdated)
                    .build();
        }

        // Make sure that we have an EPUB
        if (issueService.isEpubOutdated(issue)) {
            issueService.generateEpubIfNecessary(issue);
        }

        List<SectionImage> sectionImages = null;
        InputStream inputStream = null;
        try {
            sectionImages = issueService.getSectionImageLinks(issue);
        } catch (Exception e) {
            throw new IllegalArgumentException("Section Image Parsing error");
        } finally {
            Closeables.close(inputStream, false);
        }

        Map<String, Object> model = new HashMap<>();
        model.put("organization", issue.getPublication().getOrganization());
        model.put("publication", issue.getPublication());
        model.put("issue", issue);

        Map<String, List<URI>> imageMap = new HashMap<>();
        if (sectionImages != null) {
            for (SectionImage sectionImage : sectionImages) {
                if (!imageMap.containsKey(sectionImage.getKey())) {
                    imageMap.put(sectionImage.getKey(), new ArrayList<URI>());
                }
                imageMap.get(sectionImage.getKey()).add(
                        webserviceLocatorProvider.get().getEpubContentURI(
                                organizationId,
                                publicationId,
                                issueId,
                                sectionImage.getValue()));
            }
        }

        model.put("imageMap", imageMap);
        return Response.ok(new Viewable("/publicIssueDetail", model))
                .tag(String.valueOf(issueVersion))
                .lastModified(issueUpdated)
                .build();
    }

    /**
     * Gets an ordered list of updates that have been done to an issue. The resource returned is a paged list of updates
     * ordered by time, in the form of an <a href="http://en.wikipedia.org/wiki/Atom_(standard)">Atom</a> feed. All
     * parameters are optional, except organizationName, publicationName, and epubName. <br>
     * Query examples:
     * <ul>
     * <li>
     * </ul>
     *
     * Response body example:
     *
     * <pre class="prettyprint">
     *
     * </pre>
     *
     * Note that the each &ltentry&gt in the list contains a link to the actual issue (link with rel="alternate"), and a
     * link to the public part of the issue (link with rel="public"). The full issue can only be accessed with a valid
     * <em>access token</em>. The "public" issue resource needs no access token.
     * <p>
     * If the result list contains multiple pages (number of entries is larger than the "limit" parameter) the
     * representation will contain "next" and "prev" links to navigate to next and previous pages, respectively, as
     * specified in the Atom standard.
     *
     *
     * @param organizationId
     *            The identifier of the organization
     * @param publicationId
     *            The identifier of the publication
     * @param issueId
     *            The identifier of the isse
     * @param ifModifiedSince
     *            Return a representation only the Last-Modified time is after this time, otherwise return a 301
     *            response.
     * @param accesstoken
     *            An authorization token
     * @return
     * @throws IOException
     *             <<<<<<< HEAD if EPUB generation fails ======= if on-deman issue generation fails. >>>>>>> 4b5e6e5...
     *             CCIDIST: Public resources no longer care about the access token.
     */
    @StatusCodes({
            @ResponseCode(code = 200, condition = "OK"),
            @ResponseCode(code = 304, condition = "Not Modified"),
            @ResponseCode(code = 400, condition = "Bad Request. Response body will say what's wrong, - typically missing or invalid parameters"),
            @ResponseCode(code = 401, condition = "Unauthorized. The authorization token is missing, invalid, or expired.") })
    @ResponseHeaders({ @ResponseHeader(name = "Etag", description = "Currently unsupported."),
            @ResponseHeader(name = "Last-Modified", description = "The time of the latest update"),
            @ResponseHeader(name = "Cache-Control", description = "Currently unsupported") })
    @GET
    @Path("/{issueId}/events")
    @Produces(MediaType.APPLICATION_ATOM_XML)
    public Response getEventQueue(@PathParam("organization") @DefaultValue("") final String organizationId,
            @PathParam("publication") @DefaultValue("") final String publicationId,
            @PathParam("issueId") @DefaultValue("0") final long issueId,
            @HeaderParam("If-Modified-Since") @DefaultValue("Thu Jan 1 00:00:00 1970") String ifModifiedSince,
            @QueryParam("start") @DefaultValue("1") final int start,
            @QueryParam("limit") @DefaultValue("100") final int limit,
            @QueryParam(ACCESS_TOKEN_PARAM) @DefaultValue("") String accesstoken) throws IOException {
        Issue issue = retrieveIssue(organizationId, publicationId, issueId);

        Date issueUpdated = issue.getUpdated();
        ResponseBuilder notModifiedResponseBuilder = request.evaluatePreconditions(issueUpdated);
        if (notModifiedResponseBuilder != null) {
            return notModifiedResponseBuilder
                    .header(
                            applicationConfiguration.getProductIDHeaderName(),
                            generateProductId(issue.getPublication().getId(), issue.getId()))
                    .lastModified(issueUpdated).build();
        }

        // We can not reliably figure out the events if the EPUB is not up-to-date.
        if (issueService.isEpubOutdated(issue)) {
            issueService.generateEpubIfNecessary(issue);
        }

        Date fromDate = null;
        try {
            fromDate = HttpHeaderReader.readDate(ifModifiedSince);
            fromDate = new DateTime(DateUtils.convertDateWithTZ(fromDate)).withTimeAtStartOfDay().toDate();
        } catch (ParseException e) {
            logger.error(String.format("If-Modified-Since Header Parse exception and value is %s", ifModifiedSince));
            return Responses.clientError()
                    .entity(String.format("If-Modified-Since Header Parse exception and value is %s", ifModifiedSince))
                    .build();
        }

        ResourceLocator resourceLocator = webserviceLocatorProvider.get();
        SyndFeed feed = eventService.getChangelogAtomFeed(issue, start, limit, issue.getDriverInfo()
                .getDesignToEpubMapper()
                .getEpubName(), fromDate, "asc", resourceLocator);

        return Response
                .ok(feed)
                .header(
                        applicationConfiguration.getProductIDHeaderName(),
                        generateProductId(issue.getPublication().getId(), issue.getId()))
                .lastModified(issueUpdated).build();
    }

    /**
     * Gets an arbitrary object from an issue. The issue issue is hierarchy of hyperlinked documents, complying to the
     * Ipub 3 standard. See the <a href="http://idpf.org/epub/30">Epub 3 specification</a> for more information.
     *
     * @param organizationId
     *            The identifier of the organization
     * @param publicationId
     *            The identifier of the publication
     * @param issueId
     *            The identifier of the issue
     * @param contentLocInEpub
     *            Relative path to the content object
     * @param accesstoken
     *            An authorization token
     * @return A content object. The MIME type of the object depends on the type of object, typically it will be either
     *         metadata (XML), text (XHTML), images (png), or stylesheets (css).
     * @throws IOException
     */
    @StatusCodes({
            @ResponseCode(code = 200, condition = "OK"),
            @ResponseCode(code = 304, condition = "Not Modified. - A matching Etag was provided in the If-None-Match request header."),
            @ResponseCode(code = 400, condition = "Bad Request. - Response body contains error message"),
            @ResponseCode(code = 401, condition = "Unauthorized. The authorization token is missing, invalid, or expired.") })
    @ResponseHeaders({
            @ResponseHeader(name = "Etag", description = "The Etag stays the same as long as the issue is not updated."),
            @ResponseHeader(name = "Last-Modified", description = "Last modified date of issue."),
            @ResponseHeader(name = "Cache-Control", description = "Currently unsupported.") })
    @Path("/{issue}/{contentLocInEpub: .+}")
    @GET
    public Response getEpubContent(@PathParam("organization") @DefaultValue("") final String organizationId,
            @PathParam("publication") @DefaultValue("") final String publicationId,
            @PathParam("issue") @DefaultValue("0") final long issueId,
            @PathParam("contentLocInEpub") @DefaultValue("") final String contentLocInEpub,
            @QueryParam(ACCESS_TOKEN_PARAM) @DefaultValue("") String accesstoken) throws IOException {

        Issue issue = retrieveIssue(organizationId, publicationId, issueId);
        if (isBlank(contentLocInEpub)) {
            return Responses.clientError().entity("Content location may not be blank.").build();
        }

        // Make sure that we have an up-2-date EPUB
        if (issueService.isEpubOutdated(issue)) {
            issueService.generateEpubIfNecessary(issue);
        }

        URI fragmentPath = null;
        try {
            fragmentPath = new URI(null, null, null, -1, contentLocInEpub, null, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        boolean isPublic = isRequestedForPublicUrl(issue, fragmentPath.getPath());

        long issueUpdateUnixTs = issue.getUpdated().getTime();
        Date issueLastModified = new Date(issueUpdateUnixTs);
        EntityTag issueEtag = EntityTag.valueOf(StringUtils.createETagHeaderValue(issueUpdateUnixTs));

        ResponseBuilder notModifiedResponseBuilder = request.evaluatePreconditions(issueLastModified, issueEtag);
        if (notModifiedResponseBuilder != null) {
            ResponseBuilder responseBuilder = notModifiedResponseBuilder
                    .tag(String.valueOf(issueUpdateUnixTs))
                    .lastModified(issueLastModified);
            if (!isPublic) {
                // This is restricted URL, lets respond with a Product-ID header.
                responseBuilder = responseBuilder.header(
                        applicationConfiguration.getProductIDHeaderName(),
                        generateProductId(issue.getPublication().getId(), issue.getId()));
            }

            return responseBuilder.build();
        }

        boolean exceptionHappened = false;
        InputStream binaryStream = null;
        try {
            binaryStream = epubStorage.getFragment(issue, fragmentPath);
            StreamingOutput sout = new IssueContentStreamingOutput(binaryStream, issue.getId(), contentLocInEpub);
            String mediaType = mimeTypeConfiguration.getMediaType(contentLocInEpub);
            if (mediaType == null) {
                logger.error(String.format(
                        "TODO: Could not figure out mimeType for %s. Please add an entry to mime.properties.",
                        contentLocInEpub));
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }

            ResponseBuilder responseBuilder = Response
                    .ok(sout, mediaType)
                    .tag(String.valueOf(issueUpdateUnixTs))
                    .lastModified(issueLastModified);
            if (!isPublic) {
                // Lets add a Product-ID header
                responseBuilder = responseBuilder.header(
                        applicationConfiguration.getProductIDHeaderName(),
                        generateProductId(issue.getPublication().getId(), issue.getId()));
            }

            return responseBuilder.build();
        } catch (FileNotFoundException fnfe) {
            exceptionHappened = true;
            throw new NotFoundException(fnfe.getMessage());
        } catch (Throwable t) {
            exceptionHappened = true;
            throw t;
        } finally {
            if (exceptionHappened) {
                // We only close when exception happened. Otherwise, the StreamingOutput.write will close it.
                Closeables.close(binaryStream, true);
            }
        }
    }

    /**
     * Gets a complete issue as a single Epub 3 file.
     *
     * @param organizationId
     *            The identifier of the organization
     * @param publicationId
     *            This identifier of the publication
     * @param issueId
     *            The name if the Epub3 file. Normally this will be the same as the issue identifier
     * @param accesstoken
     *            An authorization token
     * @return An Epub 3 file
     * @throws IOException
     */
    @StatusCodes({
            @ResponseCode(code = 200, condition = "OK"),
            @ResponseCode(code = 304, condition = "Not Modified. - A matching Etag was provided in the If-None-Match request header."),
            @ResponseCode(code = 400, condition = "Bad Request. - Response body contains error message"),
            @ResponseCode(code = 401, condition = "Unauthorized. The authorization token is missing, invalid, or expired.") })
    @ResponseHeaders({
            @ResponseHeader(name = "Etag", description = "The Etag stays the same as long as the issue is not updated."),
            @ResponseHeader(name = "Last-Modified", description = "Last modified date of issue."),
            @ResponseHeader(name = "Cache-Control", description = "Currently unsupported.") })
    @GET
    @Path("/{issueId}/content.epub")
    @Produces(MediaType.TEXT_PLAIN)
    public Response downloadEpub(@PathParam("organization") @DefaultValue("") final String organizationId,
            @PathParam("publication") @DefaultValue("") final String publicationId,
            @PathParam("issueId") @DefaultValue("") final String issueId,
            @QueryParam(ACCESS_TOKEN_PARAM) @DefaultValue("") String accesstoken) throws IOException {
        Stopwatch totalTme = new Stopwatch().start();

        Issue issue = issueService.getIssue(Long.valueOf(issueId));
        checkForValidIssue(organizationId, publicationId, issue);

        // Make sure that we have an up-2-date EPUB
        if (issueService.isEpubOutdated(issue)) {
            issueService.generateEpubIfNecessary(issue);
        }

        long binaryVersion = issue.getVersion();
        ResponseBuilder unmodifiedResponseBuilder = request.evaluatePreconditions(EntityTag.valueOf(StringUtils
                .createETagHeaderValue(binaryVersion)));
        if (unmodifiedResponseBuilder != null) {
            return unmodifiedResponseBuilder.header(applicationConfiguration.getProductIDHeaderName(),
                    generateProductId(issue.getPublication().getId(), issue.getId())).build();
        }

        InputStream binaryStream = null;
        boolean exceptionHappened = false;

        try {
            binaryStream = epubStorage.get(issue);
            if (binaryStream == null) {
                exceptionHappened = true;
                throw new FileNotFoundException(String.format("No binary file for: %s", issue.getId()));
            }
            StreamingOutput streamingOutput = new IssueContentStreamingOutput(binaryStream, issue.getId(), "content.epub");
            return Response
                    .ok()
                    .header(applicationConfiguration.getProductIDHeaderName(),
                            generateProductId(issue.getPublication().getId(), issue.getId()))
                    .tag(String.valueOf(binaryVersion)).entity(streamingOutput).build();
        } catch (FileNotFoundException fnfe) {
            exceptionHappened = true;
            throw new NotFoundException(fnfe.getMessage());
        } catch (Throwable t) {
            exceptionHappened = true;
            throw t;
        } finally {
            if (exceptionHappened) {
                // We only close when exception happened. Otherwise, the StreamingOutput.write will close it.
                Closeables.close(binaryStream, exceptionHappened);
            }

            totalTme.stop();
            if (logger.isTraceEnabled()) {
                logger.trace("Total time needed for sending EPUB of Issue {}: {}", issue.getId(), totalTme);
            }
        }
    }

    private String generateProductId(String publicationId, Long issueId) {
        return new ProductID(publicationId, issueId).toString();
    }

    private Issue retrieveIssue(String organizationId, String publicationId, long issueId)
            throws WebApplicationException {
        if (isBlank(organizationId) || isBlank(publicationId) || issueId <= 0) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }

        Issue issue = issueService.getIssue(Long.valueOf(issueId));
        if (issue == null || !Objects.equals(publicationId, issue.getPublication().getId())
                || !Objects.equals(organizationId, issue.getPublication().getOrganization().getId())) {
            throw new NotFoundException();
        }

        return issue;
    }

    private void checkForValidIssue(String organizationId, String publicationId, Issue issue)
            throws WebApplicationException {
        if (isBlank(organizationId) || isBlank(publicationId)) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }

        if (publicationId == null
                || issue == null
                || !Objects.equals(publicationId, issue.getPublication().getId())
                || !Objects.equals(organizationId, issue.getPublication().getOrganization().getId())) {
            throw new NotFoundException();
        }
    }

    private boolean isRequestedForPublicUrl(Issue issue, String fragmentPath) throws IOException {
        List<SectionImage> sectionImages = null;
        try {
            sectionImages = issueService.getSectionImageLinks(issue);
        } catch (Exception e) {
            throw new IllegalArgumentException("Epub Parsing error");
        }

        if (sectionImages != null) {
            for (SectionImage sectionImage : sectionImages) {
                if (fragmentPath.equals(sectionImage.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }
}
