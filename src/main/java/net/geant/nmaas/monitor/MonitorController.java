package net.geant.nmaas.monitor;

import java.util.List;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.monitor.model.MonitorEntryView;
import net.geant.nmaas.scheduling.ScheduleManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/monitor")
public class MonitorController {

    private final MonitorManager monitorManager;

    private final ScheduleManager scheduleManager;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR')")
    public void createMonitorEntryAndJob(@RequestBody MonitorEntryView monitorEntryView) {
        this.scheduleManager.createJob(monitorEntryView);
        this.monitorManager.createMonitorEntry(monitorEntryView);
    }

    @PostMapping("/{serviceName}/execute")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR')")
    public void executeJobNow(@PathVariable String serviceName){
        scheduleManager.executeJob(serviceName);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR')")
    public void updateMonitorEntryAndJob(@RequestBody MonitorEntryView monitorEntryView) {
        this.scheduleManager.updateJob(monitorEntryView);
        this.monitorManager.updateMonitorEntry(monitorEntryView);
    }

    @DeleteMapping("/{serviceName}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR')")
    public void deleteMonitorEntryAndJob(@PathVariable String serviceName){
        this.scheduleManager.deleteJob(serviceName);
        this.monitorManager.deleteMonitorEntry(serviceName);
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public List<MonitorEntryView> getAllMonitorEntries(){
        return this.monitorManager.getAllMonitorEntries();
    }

    @GetMapping("/{serviceName}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR')")
    public MonitorEntryView getMonitorEntry(@PathVariable String serviceName){
        return this.monitorManager.getMonitorEntries(serviceName);
    }

    @PatchMapping("/{serviceName}/resume")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR')")
    public void resumeJob(@PathVariable String serviceName){
        scheduleManager.resumeJob(serviceName);
        monitorManager.changeJobState(serviceName, true);
    }

    @PatchMapping("/{serviceName}/pause")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR')")
    public void pauseJob(@PathVariable String serviceName){
        scheduleManager.pauseJob(serviceName);
        monitorManager.changeJobState(serviceName, false);
    }
}
