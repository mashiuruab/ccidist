package com.cefalo.cci.restResource;

import static com.cefalo.cci.utils.StringUtils.isBlank;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.inject.Named;
import javax.inject.Provider;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.ResponseHeader;
import org.codehaus.enunciate.jaxrs.ResponseHeaders;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.model.DesignToEpubMapper;
import com.cefalo.cci.model.Issue;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.model.RxmlZipFile;
import com.cefalo.cci.service.DriverInfoService;
import com.cefalo.cci.service.IssueService;
import com.cefalo.cci.service.PublicationService;
import com.cefalo.cci.service.RxmlService;
import com.cefalo.cci.service.UsersService;
import com.cefalo.cci.utils.DateUtils;
import com.cefalo.cci.utils.FileUtils;
import com.cefalo.cci.utils.StringUtils;
import com.cefalo.cci.utils.XpathHelper;
import com.cefalo.cci.utils.locator.ResourceLocator;
import com.google.inject.Inject;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.view.Viewable;

/**
 * This class provides a resource to upload new issues and update existing issues. The format of the issue is a CCI
 * Object XML file (aka RXML file). The complete issue file is uploaded both both when the issue is initially uploaded,
 * and when it is later updated.
 */
@Path("/edit/{organization}/{publication}/issues/")
public class AdminIssueResource {
    private static String DESIGN_NAME_EXP = "CCIObjects/object/attributes/attribute[@name='DesignName']";
    private static String PRODUCT_NAME_CONT_EXP = "CCIObjects/object/attributes/attribute[@name='ProductNameCont']";
    private static String PUB_DATE_CONT_EXP = "CCIObjects/object/attributes/attribute[@name='PubDateCont']";
    private static String PUB_DATE_DATE_EXP = "CCIObjects/object/attributes/attribute[@name='PubDateDate']";

    private final Logger logger = LoggerFactory.getLogger(AdminIssueResource.class);

    @Inject
    private PublicationService publicationService;

    @Inject
    private IssueService issueService;

    @Inject
    private RxmlService rxmlService;

    @Inject
    private DriverInfoService driverInfoService;

    @Context
    private Request request;

    @Inject
    private UsersService usersService;

    @Inject
    private ApplicationConfiguration config;

    @Inject
    @Named("webservice")
    private Provider<ResourceLocator> webserviceLocatorProvider;

    /**
     * Creates a new issue or updates an existing issue. The uploaded file contains the complete issue, both when a new
     * issue is created, and when an existing issue is updated.
     * <p>
     * The issue identifier and design name are extracted from the manifest inside the CCI Object XML file. The
     * Distribution Server will consider this an update if a file with the same issue id/ design name combination
     * already exists, - otherwise this will be considered a create operation.
     * <p>
     * This resource requires user name and password (basic authentication).
     *
     * @param organizationId
     *            The id of the organization
     * @param publicationId
     *            The id odf the publication
     * @param authorization
     *            User name and password, bas64 encoded
     * @param uploadedBinaryStream
     *            The CCI Object XML file to be uploaded, as the body of the POST message
     * @return A response document with URLs to the newly created resources
     * @throws IOException
     */
    @StatusCodes({
            @ResponseCode(code = 200, condition = "OK"),
            @ResponseCode(code = 400, condition = "Bad Request. The response body contains an error message."),
            @ResponseCode(code = 401, condition = "Unauthorized. User name and password missing or wrong.")
    })
    @ResponseHeaders({
            @ResponseHeader(name = "WWW-Authenticate", description = "Identifies authentication method and realm.")
    })
    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_XHTML_XML)
    @Path("/cciobjectxml/")
    public Response uploadRxmlFile(
            @PathParam("organization") @DefaultValue("") final String organizationId,
            @PathParam("publication") @DefaultValue("") final String publicationId,
            final InputStream uploadedBinaryStream,
            @HeaderParam("Authorization") final String authorization) throws Exception {
        // Temporary file to write out the uploaded RXML file.
        File uploadedRxmlFile = new File(config.getTmpDir(), UUID.randomUUID().toString());

        try (InputStream inputStream = uploadedBinaryStream) {
            checkForValidPublication(organizationId, publicationId);
            if (!usersService.hasRole(publicationId, Arrays.asList("Ingester"))) {
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            }
            FileUtils.writeToFile(uploadedBinaryStream, uploadedRxmlFile);

            RxmlZipFile rxmlObject = createRxmlObject(uploadedRxmlFile, organizationId, publicationId);

            Response.Status responseStatus;
            RxmlZipFile rxmlZipFile = rxmlService.getRxmlFileByName(rxmlObject.getFileName(), publicationId);
            if (rxmlZipFile == null) {
                rxmlObject.setPublication(publicationService.getPublication(publicationId));
                rxmlObject.setCreated(DateUtils.convertDateWithTZ(new Date()));

                issueService.createRxmlFileWithIssues(rxmlObject, uploadedRxmlFile);
                rxmlZipFile = rxmlObject;

                responseStatus = Response.Status.CREATED;
            } else {
                // FIXME: Do we need to validate here? All the uploaded attributes to the existing ones???
                Response.ResponseBuilder ifMatchResponseBuilder = request.evaluatePreconditions(EntityTag
                        .valueOf(StringUtils
                                .createETagHeaderValue(rxmlZipFile.getVersion())));
                if (ifMatchResponseBuilder != null) {
                    FileUtils.deleteRecursive(uploadedRxmlFile);
                    return ifMatchResponseBuilder.tag(String.valueOf(rxmlZipFile.getVersion())).build();
                }

                issueService.updateRXMlFileWithIssues(rxmlZipFile, uploadedRxmlFile);
                responseStatus = Response.Status.OK;
            }

            List<Issue> issueList = issueService.getIssueListByZipFileId(rxmlZipFile.getId());
            Map<String, String> issueLinkMap = new HashMap<>();
            for (Issue issue : issueList) {
                issueLinkMap.put(
                        issue.getDriverInfo().getDesignToEpubMapper().getEpubName(),
                        webserviceLocatorProvider.get().getIssueURI(
                                organizationId,
                                publicationId,
                                issue.getId()).toString());
            }

            Map<String, Object> model = new HashMap<>();
            model.put("issueLinkMap", issueLinkMap);
            return Response.ok(new Viewable("/issueList", model)).status(responseStatus).build();
        } catch (IllegalArgumentException iae) {
            logger.error("Client Error while processing uploaded RXML file.", iae);
            throw new WebApplicationException(iae, Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Server Error while trying to process uploaded RXML file.", e);
            throw e;
        } finally {
            FileUtils.deleteRecursive(uploadedRxmlFile);
        }
    }

    private RxmlZipFile createRxmlObject(
            File rxmlFile,
            String organizationId,
            String publicationId) throws Exception {
        try (InputStream editionInputStream = FileUtils.getExtractedContent(rxmlFile, "Edition.xml")) {
            XpathHelper xpathHelper = new XpathHelper(editionInputStream);

            String designNameStr = xpathHelper.parseSingleNodeValue(DESIGN_NAME_EXP);
            String productNameContStr = xpathHelper.parseSingleNodeValue(PRODUCT_NAME_CONT_EXP);
            String pubDateContStr = xpathHelper.parseSingleNodeValue(PUB_DATE_CONT_EXP);
            String pubDateDateStr = xpathHelper.parseSingleNodeValue(PUB_DATE_DATE_EXP);

            if (isBlank(productNameContStr)) {
                throw new IllegalArgumentException(String.format(
                        "CciObjectXml File is not valid as ProductNameCont is %s.", productNameContStr));
            } else if (isBlank(pubDateContStr)) {
                throw new IllegalArgumentException(String.format(
                        "CciObjectXml File is not valid as PubDateCont is %s.", pubDateContStr));
            } else if (isBlank(pubDateDateStr)) {
                throw new IllegalArgumentException(String.format(
                        "CciObjectXml File is not valid as PubDateDate is %s.", pubDateDateStr));
            }
            else if (!isValidDesign(designNameStr, publicationId)) {
                throw new IllegalArgumentException(String.format("CciObjectXml File is not valid as DesignName is %s",
                        designNameStr));
            }

            RxmlZipFile rxmlObject = new RxmlZipFile();
            rxmlObject.setFileName(
                    String.format("%s_%s_%s_%s",
                            organizationId, productNameContStr, pubDateContStr, designNameStr));
            rxmlObject.setDesignName(designNameStr);
            rxmlObject.setIssueName(pubDateContStr);
            rxmlObject.setIssueDate(DateUtils.convertStringMetaDate(pubDateDateStr));

            return rxmlObject;
        }
    }

    private boolean isValidDesign(String designName, String publicationId) {
        List<DesignToEpubMapper> designToEpubMapperList = driverInfoService.getDesignByPublicationId(designName,
                publicationId, DateUtils.convertDateWithTZ(new Date()));
        return !designToEpubMapperList.isEmpty();
    }

    private void checkForValidPublication(String organizationId, String publicationId) throws WebApplicationException {
        if (isBlank(organizationId) || isBlank(publicationId)) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        Publication publication = publicationService.getPublication(publicationId);
        if (publication == null || !Objects.equals(publicationId, publication.getId())
                || !Objects.equals(organizationId, publication.getOrganization().getId())) {
            throw new NotFoundException();
        }
    }

}
