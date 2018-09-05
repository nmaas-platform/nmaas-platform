package net.geant.nmaas.externalservices.monitor.scheduling;

import java.util.Arrays;
import java.util.List;
import net.geant.nmaas.externalservices.inventory.gitlab.GitLabManager;
import net.geant.nmaas.externalservices.inventory.gitlab.GitLabMonitorServiceImpl;
import net.geant.nmaas.externalservices.monitor.MonitorManager;
import net.geant.nmaas.externalservices.monitor.MonitorService;
import net.geant.nmaas.externalservices.monitor.ServiceType;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.TriggerKey;

public class SchedulingManagerTest {

    private List<MonitorService> monitorServices;

    private GitLabManager gitLabManager = mock(GitLabManager.class);

    private MonitorManager monitorManager = mock(MonitorManager.class);

    private Scheduler scheduler = mock(Scheduler.class);

    private ScheduleManager scheduleManager;

    private JobDescriptor jobDescriptor;

    @Before
    public void setup() throws Exception{
        jobDescriptor = new JobDescriptor(ServiceType.GITLAB, 3L);
        GitLabMonitorServiceImpl gitLabMonitorService = new GitLabMonitorServiceImpl();
        gitLabMonitorService.setGitLabManager(gitLabManager);
        gitLabMonitorService.setMonitorManager(monitorManager);
        monitorServices = Arrays.asList(gitLabMonitorService);
        scheduleManager = new ScheduleManager(monitorServices, scheduler);
        when(scheduler.checkExists(JobKey.jobKey(ServiceType.GITLAB.getName()))).thenReturn(false);
    }

    @Test
    public void shouldCreateJob() throws Exception{
        JobDescriptor result = this.scheduleManager.createJob(jobDescriptor);
        assertThat("Job mismatch", result.getServiceName().equals(jobDescriptor.getServiceName()));
        assertThat("Interval mismatch", result.getCheckInterval().equals(jobDescriptor.getCheckInterval()));
        verify(scheduler, times(1)).scheduleJob(any(), anySet(), anyBoolean());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotCreateJobWhenJobExists() throws Exception{
        when(scheduler.checkExists(JobKey.jobKey(ServiceType.GITLAB.getName()))).thenReturn(true);
        JobDescriptor result = this.scheduleManager.createJob(jobDescriptor);
    }

    @Test
    public void shouldUpdateJob() throws Exception{
        when(scheduler.getTrigger(TriggerKey.triggerKey(ServiceType.GITLAB.getName()))).thenReturn(jobDescriptor.buildTrigger());
        this.scheduleManager.updateJob(jobDescriptor);
        verify(scheduler, times(1)).rescheduleJob(any(), any());
    }

    @Test
    public void shouldNotUpdate() throws Exception{
        when(scheduler.getTrigger(TriggerKey.triggerKey(ServiceType.GITLAB.getName()))).thenReturn(null);
        this.scheduleManager.updateJob(jobDescriptor);
        verify(scheduler, times(0)).rescheduleJob(any(), any());
    }



}
