package net.geant.nmaas.externalservices.api;

import net.geant.nmaas.externalservices.monitor.scheduling.ScheduleManager;
import net.geant.nmaas.externalservices.monitor.scheduling.JobDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {
    private ScheduleManager scheduleManager;

    @Autowired
    public ScheduleController(ScheduleManager scheduleManager){
        this.scheduleManager = scheduleManager;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_OPERATOR')")
    public void createJob(@RequestBody JobDescriptor jobDescriptor){
        scheduleManager.createJob(jobDescriptor);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_OPERATOR')")
    public void updateJob(@RequestBody JobDescriptor jobDescriptor){
        scheduleManager.updateJob(jobDescriptor);
    }

    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_OPERATOR')")
    public void deleteJob(@PathVariable String name){
        scheduleManager.deleteJob(name);
    }

    @DeleteMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_OPERATOR')")
    public void deleteAllJobs(){
        scheduleManager.deleteAllJobs();
    }

    @PatchMapping("/{name}/resume")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_OPERATOR')")
    public void resumeJob(@PathVariable String name){
        scheduleManager.resumeJob(name);
    }

    @PatchMapping("/{name}/pause")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_OPERATOR')")
    public void pauseJob(@PathVariable String name){
        scheduleManager.pauseJob(name);
    }

}
