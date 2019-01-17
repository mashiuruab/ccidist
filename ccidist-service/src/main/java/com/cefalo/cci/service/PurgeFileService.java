package com.cefalo.cci.service;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.model.Issue;
import com.cefalo.cci.model.RxmlZipFile;
import com.cefalo.cci.storage.Storage;
import com.cefalo.cci.utils.DateUtils;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

@javax.inject.Singleton
@org.nnsoft.guice.guartz.Scheduled(jobName = "test", cronExpression = "0 0 0 * * ?")
public class PurgeFileService implements org.quartz.Job {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private IssueService issueService;

    @Inject
    private Storage<Issue> epubStorage;

    @Inject
    private Storage<RxmlZipFile> rxmlStorage;

    @Inject
    private ApplicationConfiguration config;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if (logger.isInfoEnabled()) {
            logger.info("Cron job to cleanup cache directory started.");
        }

        long purgeTime = new DateTime().minusDays(config.getCachedEpubPurgeAge()).getMillis();
        List<Issue> purgableIssues = issueService.getOldIssueList(DateUtils.convertDateWithTZ(new Date(purgeTime)));

        for (Issue issue : purgableIssues) {
            epubStorage.cleanUp(issue);

            // We'll also delete the RXML files.
            rxmlStorage.cleanUp(issue.getRxmlZipFile());
        }
    }

}
