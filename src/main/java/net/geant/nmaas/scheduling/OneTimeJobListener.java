package net.geant.nmaas.scheduling;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DateBuilder;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

@RequiredArgsConstructor
@Slf4j
public class OneTimeJobListener implements JobListener {
    private final Scheduler scheduler;
    private final JobKey jobKey;

    @Override
    public String getName() {
        return "OneTimeJobListener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {}

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {}

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        if (jobException != null) {
            log.error("Job {} failed. Retrying in 15 minutes.", jobKey, jobException);
            reScheduleJob(context);
        } else {
            try {
                scheduler.deleteJob(jobKey);
            } catch (SchedulerException e) {
                log.trace("Failed to delete job {}", jobKey, e);
            }
        }
    }

    private void reScheduleJob(JobExecutionContext context) {
        try {
            Trigger retryTrigger = TriggerBuilder.newTrigger()
                    .forJob(context.getJobDetail())
                    .startAt(DateBuilder.futureDate(15, DateBuilder.IntervalUnit.MINUTE))
                    .build();

            scheduler.scheduleJob(retryTrigger);
        } catch (SchedulerException e) {
            reScheduleJob(context);
        }
    }
}