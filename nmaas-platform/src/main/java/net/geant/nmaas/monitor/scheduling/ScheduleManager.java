package net.geant.nmaas.monitor.scheduling;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.geant.nmaas.monitor.MonitorService;
import net.geant.nmaas.monitor.exceptions.MonitorServiceNotFound;
import static org.quartz.JobBuilder.newJob;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import static org.quartz.JobKey.jobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ScheduleManager {
    private List<MonitorService> monitorServices;

    private Scheduler scheduler;

    @Autowired
    public ScheduleManager(List<MonitorService> monitorServices, Scheduler scheduler){
        this.monitorServices = monitorServices;
        this.scheduler = scheduler;
    }

    public JobDescriptor createJob(JobDescriptor jobDescriptor){
        validateJobDescriptor(jobDescriptor);
        try{
            if(scheduler.checkExists(jobKey(jobDescriptor.getServiceName().getName())))
                throw new IllegalStateException(String.format("Job with name %s already exists", jobDescriptor.getServiceName()));
            MonitorService service = monitorServices.stream().filter(s->s.getServiceType().getName().equals(jobDescriptor.getServiceName().getName()))
                    .findAny().orElseThrow(() -> new MonitorServiceNotFound(String.format("Monitor service for %s not found", jobDescriptor.getServiceName().getName())));
            JobDetail jobDetail = newJob(service.getClass()).withIdentity(jobDescriptor.getServiceName().getName()).build();
            Trigger trigger = jobDescriptor.buildTrigger();
            scheduler.scheduleJob(jobDetail, ImmutableSet.of(trigger), false);
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

    public void updateJob(JobDescriptor jobDescriptor){
        validateJobDescriptor(jobDescriptor);
        try{
            Trigger trigger = scheduler.getTrigger(TriggerKey.triggerKey(jobDescriptor.getServiceName().getName()));
            if(trigger != null){
                trigger = jobDescriptor.buildTrigger();
                scheduler.rescheduleJob(TriggerKey.triggerKey(jobDescriptor.getServiceName().getName()), trigger);
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
            scheduler.deleteJobs(new ArrayList<JobKey>(keys));
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

}
