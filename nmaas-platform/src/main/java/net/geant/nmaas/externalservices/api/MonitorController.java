package net.geant.nmaas.externalservices.api;

import java.util.List;
import net.geant.nmaas.externalservices.api.model.MonitorEntryView;
import net.geant.nmaas.externalservices.monitor.MonitorManager;
import net.geant.nmaas.externalservices.monitor.ServiceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    private MonitorManager monitorManager;

    @Autowired
    public MonitorController(MonitorManager monitorManager){
        this.monitorManager = monitorManager;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_OPERATOR')")
    public void createMonitorEntry(@RequestBody MonitorEntryView monitorEntryView){
        this.monitorManager.createMonitorEntry(monitorEntryView);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_OPERATOR')")
    public void updateMonitorEntry(@RequestBody MonitorEntryView monitorEntryView){
        this.monitorManager.updateMonitorEntry(monitorEntryView);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_OPERATOR')")
    public void deleteMonitorEntry(@RequestBody MonitorEntryView monitorEntryView){
        this.monitorManager.deleteMonitorEntry(monitorEntryView);
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_OPERATOR')")
    public List<MonitorEntryView> getAllMonitorEntries(){
        return this.monitorManager.getAllMonitorEntities();
    }

    @GetMapping("/{serviceName}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_OPERATOR')")
    public MonitorEntryView getMonitorEntry(@PathVariable String serviceName){
        return this.monitorManager.getMonitorEntity(ServiceType.valueOf(serviceName));
    }
}
