package com.cefalo.cci.config;

import java.util.Properties;

import com.cefalo.cci.utils.PropertyUtils;

public class VersionManager {
    private final String version;

    private final String gitCommitDescription;
    private final String gitCommitId;
    private final String gitCommitTime;

    public VersionManager() {
        Properties versionProps = PropertyUtils.readPropertiesFile("/META-INF/Version.properties");

        // This is our intended version
        this.version = (String) versionProps.remove("version");

        // Git related information. These will help to pinpoint the exact source code state.
        this.gitCommitDescription = (String) versionProps.remove("git.commit.description");
        this.gitCommitId = (String) versionProps.remove("git.commit.id");
        this.gitCommitTime = (String) versionProps.remove("git.commit.time");
    }

    public String getVersion() {
        return version;
    }

    public String getGitCommitDescription() {
        return gitCommitDescription;
    }

    public String getGitCommitId() {
        return gitCommitId;
    }

    public String getGitCommitTime() {
        return gitCommitTime;
    }
}
