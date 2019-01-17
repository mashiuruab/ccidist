package com.cefalo.cci.restResource;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.config.VersionManager;
import com.google.gson.JsonObject;

@Path("/version")
public class VersionResource {
    @Inject
    private ApplicationConfiguration config;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getVersionInfo() {
        VersionManager versionManager = config.getVersionManager();

        JsonObject versionJson = new JsonObject();
        versionJson.addProperty("version", versionManager.getVersion());

        JsonObject buildInfo = new JsonObject();
        buildInfo.addProperty("commitId", versionManager.getGitCommitId());
        buildInfo.addProperty("commitDescription", versionManager.getGitCommitDescription());
        buildInfo.addProperty("commitTime", versionManager.getGitCommitTime());
        versionJson.add("buildInfo", buildInfo);

        return versionJson.toString();
    }
}
