package net.geant.nmaas.scheduling;

import net.geant.nmaas.externalservices.gitlab.GitLabManager;
import net.geant.nmaas.externalservices.gitlab.GitLabMonitorService;
import net.geant.nmaas.monitor.MonitorManager;
import net.geant.nmaas.monitor.ServiceType;
import net.geant.nmaas.monitor.TimeFormat;
import net.geant.nmaas.monitor.model.MonitorEntryView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.TriggerKey;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SchedulingManagerTest {

    private final GitLabManager gitLabManager = mock(GitLabManager.class);
    private final MonitorManager monitorManager = mock(MonitorManager.class);
    private final Scheduler scheduler = mock(Scheduler.class);
    private final MonitorEntryView monitorEntryView = new MonitorEntryView(ServiceType.GITLAB, 3L, TimeFormat.MIN);

    private GitLabMonitorService gitLabMonitorService;

    private ScheduleManager scheduleManager;

    @BeforeEach
    public void setup() throws Exception{
        gitLabMonitorService = new GitLabMonitorService();
        gitLabMonitorService.setGitLabManager(gitLabManager);
        gitLabMonitorService.setMonitorManager(monitorManager);
        scheduleManager = new ScheduleManager( scheduler);
        when(scheduler.checkExists(JobKey.jobKey(ServiceType.GITLAB.getName()))).thenReturn(false);
    }

    @Test
    public void shouldCreateJob() throws Exception {
        this.scheduleManager.createJob(this.gitLabMonitorService, monitorEntryView);
        verify(scheduler, times(1)).scheduleJob(any(), anySet(), anyBoolean());
    }

    @Test
    public void shouldNotCreateJobWhenJobExists() {
        assertThrows(IllegalStateException.class, () -> {
            when(scheduler.checkExists(JobKey.jobKey(ServiceType.GITLAB.getName()))).thenReturn(true);
            this.scheduleManager.createJob(this.gitLabMonitorService, monitorEntryView);
        });
    }

    @Test
    public void shouldUpdateJob() throws Exception {
        JobDescriptor jobDescriptor = new JobDescriptor(ServiceType.GITLAB, 3L, TimeFormat.MIN);
        when(scheduler.getTrigger(TriggerKey.triggerKey(ServiceType.GITLAB.getName()))).thenReturn(jobDescriptor.buildTrigger());
        this.scheduleManager.updateJob(monitorEntryView);
        verify(scheduler, times(1)).rescheduleJob(any(), any());
    }

    @Test
    public void shouldNotUpdate() throws Exception {
        when(scheduler.getTrigger(TriggerKey.triggerKey(ServiceType.GITLAB.getName()))).thenReturn(null);
        this.scheduleManager.updateJob(monitorEntryView);
        verify(scheduler, times(0)).rescheduleJob(any(), any());
    }

}
