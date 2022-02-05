package net.geant.nmaas.scheduling;

import net.geant.nmaas.externalservices.inventory.gitlab.GitLabManager;
import net.geant.nmaas.externalservices.inventory.gitlab.GitLabMonitorService;
import net.geant.nmaas.monitor.MonitorManager;
import net.geant.nmaas.monitor.MonitorService;
import net.geant.nmaas.monitor.ServiceType;
import net.geant.nmaas.monitor.TimeFormat;
import net.geant.nmaas.monitor.exceptions.MonitorServiceNotFound;
import net.geant.nmaas.monitor.model.MonitorEntryView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.TriggerKey;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
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

    private ScheduleManager scheduleManager;

    @BeforeEach
    public void setup() throws Exception{
        GitLabMonitorService gitLabMonitorService = new GitLabMonitorService();
        gitLabMonitorService.setGitLabManager(gitLabManager);
        gitLabMonitorService.setMonitorManager(monitorManager);
        List<MonitorService> monitorServices = Collections.singletonList(gitLabMonitorService);
        scheduleManager = new ScheduleManager(monitorServices, scheduler);
        when(scheduler.checkExists(JobKey.jobKey(ServiceType.GITLAB.getName()))).thenReturn(false);
    }

    @Test
    public void shouldCreateJob() throws Exception {
        JobDescriptor result = this.scheduleManager.createJob(monitorEntryView);
        assertThat("Job mismatch", result.getServiceName().equals(monitorEntryView.getServiceName()));
        assertThat("Interval mismatch", result.getCheckInterval().equals(monitorEntryView.getCheckInterval()));
        verify(scheduler, times(1)).scheduleJob(any(), anySet(), anyBoolean());
    }

    @Test
    public void shouldNotCreateJobWhenJobExists() {
        assertThrows(IllegalStateException.class, () -> {
            when(scheduler.checkExists(JobKey.jobKey(ServiceType.GITLAB.getName()))).thenReturn(true);
            this.scheduleManager.createJob(monitorEntryView);
        });
    }

    @Test
    public void shouldExecuteJobWithCorrectName() {
        this.scheduleManager.executeJob("GITLAB");
        verify(monitorManager, times(1)).updateMonitorEntry(any(), any(), any());
    }

    @Test
    public void shouldExecuteJobWhenNameIsCorrectButIsNotUpperCase() {
        this.scheduleManager.executeJob("GiTLaB");
        verify(monitorManager, times(1)).updateMonitorEntry(any(), any(), any());
    }

    @Test
    public void shouldNotExecuteJobWhenServiceCannotBeFound() {
        assertThrows(MonitorServiceNotFound.class, () -> {
            this.scheduleManager.executeJob("GITHUB");
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
