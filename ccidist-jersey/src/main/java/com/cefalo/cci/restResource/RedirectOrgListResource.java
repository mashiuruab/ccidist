package com.cefalo.cci.restResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

@Path("/")
public class RedirectOrgListResource {
    @Context
    private UriInfo uriInfo;

    @GET
    public Response redirectToOrgList() {
        URI uri = uriInfo.getBaseUriBuilder().path("orgList.action").build();
        return Response.seeOther(uri).build();
    }
}
