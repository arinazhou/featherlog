package com.arinazhou.featherlog.care;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "featherlog.reminders.enabled", havingValue = "true", matchIfMissing = true)
public class CareReminderJob {

    private static final Logger log = LoggerFactory.getLogger(CareReminderJob.class);

    private final CareTaskService careTaskService;

    public CareReminderJob(CareTaskService careTaskService) {
        this.careTaskService = careTaskService;
    }

    @Scheduled(cron = "${featherlog.reminders.cron}")
    public void run() {
        int opened = careTaskService.raiseOverdueAlerts();
        if (opened > 0) {
            log.info("Opened {} overdue care alert(s)", opened);
        }
    }
}
