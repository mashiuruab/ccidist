package com.cefalo.cci.config;

import java.util.Properties;

public class ApplicationConfiguration {
    public static final String JAVA_MELODY_SYSTEM_PROP = "com.cefalo.enableJavaMelody";

    private final int cachedEpubPurgeAge;
    private final String cacheDirectoryPath;
    private final String authenticationSecret;
    private final String productHeader;
    private final boolean skipTokenAuthentication;
    private final String webserviceURL;
    private final String adminURL;
    private final String digitaldriverURL;
    private final String externalAdminURL;
    private final long tokenValidityDuration;
    private final String redirectURLWithQueryKey;
    private final String tmpDir;

    private final VersionManager versionManager;

    public ApplicationConfiguration(Properties configuredProperties) {
        this.cachedEpubPurgeAge = Integer.valueOf((String) configuredProperties.get("cachedEpubPurgeAge"));
        this.cacheDirectoryPath = (String) configuredProperties.get("cacheDirectory");
        this.authenticationSecret = (String) configuredProperties.get("authenticationSecret");
        this.productHeader = (String) configuredProperties.get("productHeader");
        this.skipTokenAuthentication = Boolean.valueOf((String) configuredProperties.get("skipTokenAuthentication"));
        this.webserviceURL = (String) configuredProperties.get("webserviceURL");
        this.adminURL = (String) configuredProperties.get("adminURL");
        this.digitaldriverURL = (String) configuredProperties.get("digitaldriverURL");
        this.externalAdminURL = (String) configuredProperties.get("externalAdminURL");
        this.tokenValidityDuration = Long.valueOf((String) configuredProperties.get("tokenValidityDuration"));
        this.redirectURLWithQueryKey = (String) configuredProperties.get("redirectURLWithQueryKey");

        System.setProperty(JAVA_MELODY_SYSTEM_PROP, (String) configuredProperties.get("enableMonitoring"));
        this.tmpDir = System.getProperty("java.io.tmpdir");

        this.versionManager = new VersionManager();
    }

    public boolean isMonitoringEnabled() {
        return Boolean.parseBoolean(System.getProperty(JAVA_MELODY_SYSTEM_PROP, "true"));
    }

    public int getCachedEpubPurgeAge() {
        return cachedEpubPurgeAge;
    }

    public String getAuthenticationSecret() {
        return authenticationSecret;
    }

    public String getCacheDirectoryPath() {
        return cacheDirectoryPath;
    }

    public long getTokenValidityDuration() {
        return tokenValidityDuration;
    }

    public String getProductIDHeaderName() {
        return productHeader;
    }

    public boolean skipTokenBasedAuthentication() {
        return skipTokenAuthentication;
    }

    public String getWebserviceURL() {
        return webserviceURL;
    }

    public String getAdminURL() {
        return adminURL;
    }

    public String getDigitaldriverURL() {
        return digitaldriverURL;
    }

    public String getExternalAdminURL() {
        return externalAdminURL;
    }

    public String getRedirectURLWithQueryKey() {
        return redirectURLWithQueryKey;
    }

    public String getTmpDir() {
        return tmpDir;
    }

    public VersionManager getVersionManager() {
        return versionManager;
    }
}
