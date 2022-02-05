package net.geant.nmaas.scheduling;

import com.google.common.collect.ImmutableSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.monitor.MonitorService;
import net.geant.nmaas.monitor.exceptions.MonitorServiceNotFound;
import net.geant.nmaas.monitor.model.MonitorEntryView;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobKey.jobKey;

@Component
@RequiredArgsConstructor
@Log4j2
public class ScheduleManager {

    private final List<MonitorService> monitorServices;

    private final Scheduler scheduler;

    public JobDescriptor createJob(MonitorEntryView monitorEntryView){
        JobDescriptor jobDescriptor = new JobDescriptor(monitorEntryView.getServiceName(), monitorEntryView.getCheckInterval(), monitorEntryView.getTimeFormat());
        validateJobDescriptor(jobDescriptor);
        try{
            if(scheduler.checkExists(jobKey(jobDescriptor.getServiceName().getName()))) {
                log.error(String.format("Job with name %s already exists", jobDescriptor.getServiceName()));
                throw new IllegalStateException(String.format("Job with name %s already exists", jobDescriptor.getServiceName()));
            } else {
                MonitorService service = monitorServices.stream().filter(s->s.getServiceType().getName().equals(jobDescriptor.getServiceName().getName()))
                        .findAny().orElseThrow(() -> new MonitorServiceNotFound(String.format("Monitor service for %s not found", jobDescriptor.getServiceName().getName())));
                JobDetail jobDetail = newJob(service.getClass()).withIdentity(jobDescriptor.getServiceName().getName()).build();
                Trigger trigger = jobDescriptor.buildTrigger();
                log.info("Scheduling job: " + jobDescriptor.getServiceName().toString());
                scheduler.scheduleJob(jobDetail, ImmutableSet.of(trigger), false);
            }
        } catch (SchedulerException e){
            throw new IllegalStateException(e.getMessage());
        }
        return jobDescriptor;
    }

    public void executeJob(String name){
        String serviceName = name.toUpperCase();
        MonitorService service = monitorServices.stream().filter(s->s.getServiceType().getName().equals(serviceName))
                .findAny().orElseThrow(() -> new MonitorServiceNotFound(String.format("Monitor service for %s not found", serviceName)));
        service.checkStatus();
    }

    public void updateJob(MonitorEntryView monitorEntryView){
        JobDescriptor jobDescriptor = new JobDescriptor(monitorEntryView.getServiceName(), monitorEntryView.getCheckInterval(), monitorEntryView.getTimeFormat());
        validateJobDescriptor(jobDescriptor);
        try{
            Trigger trigger = scheduler.getTrigger(TriggerKey.triggerKey(jobDescriptor.getServiceName().getName()));
            if(trigger != null){
                trigger = jobDescriptor.buildTrigger();
                scheduler.rescheduleJob(TriggerKey.triggerKey(jobDescriptor.getServiceName().getName()), trigger);
                if(!monitorEntryView.isActive()){
                    this.pauseJob(trigger.getJobKey().getName());
                }
            }
        } catch (SchedulerException e){
            throw new IllegalStateException("Updating job failed due to " + e.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteJob(String name){
        try{
            scheduler.deleteJob(jobKey(name));
        } catch (SchedulerException e){
            throw new IllegalStateException("Deleting scheduled job failed due to " + e.getMessage());
        }
    }

    public void deleteAllJobs(){
        try{
            Set<JobKey> keys = scheduler.getJobKeys(GroupMatcher.anyJobGroup());
            scheduler.deleteJobs(new ArrayList<>(keys));
        } catch(SchedulerException e){
            throw new IllegalStateException("Deleting all scheduled jobs failed due to " + e.getMessage());
        }
    }

    public void pauseJob(String name){
        try{
            scheduler.pauseJob(jobKey(name));
        } catch(SchedulerException e){
            throw new IllegalStateException(String.format("Pausing job %s failed due to %s", name, e.getMessage()));
        }
    }

    public void resumeJob(String name){
        try{
            scheduler.resumeJob(jobKey(name));
        } catch(SchedulerException e){
            throw new IllegalStateException(String.format("Resuming job %s failed due to %s", name, e.getMessage()));
        }
    }

    private void validateJobDescriptor(JobDescriptor jobDescriptor){
        if(jobDescriptor.getServiceName() == null)
            throw new IllegalStateException("Service name cannot be null");
        if(jobDescriptor.getCheckInterval() == null || jobDescriptor.getCheckInterval() <= 0)
            throw new IllegalStateException("Check interval cannot be less or equal 0");
    }

    public boolean jobExists(String name) {
        try {
            return scheduler.checkExists(jobKey(name));
        } catch (SchedulerException e) {
            log.warn(String.format("Exception caught (%s)", e.getMessage()));
            log.warn(e.getStackTrace());
        }
        return false;
    }

}
