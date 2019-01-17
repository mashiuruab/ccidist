package com.cefalo.cci.mapping;

import com.cefalo.cci.service.PurgeFileService;
import org.nnsoft.guice.guartz.QuartzModule;

public class PurgeFileQuartzModule extends QuartzModule {
    @Override
    protected void schedule() {
        configureScheduler().withManualStart();
        scheduleJob(PurgeFileService.class);
    }
}
