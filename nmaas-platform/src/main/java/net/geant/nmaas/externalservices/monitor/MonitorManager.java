package net.geant.nmaas.externalservices.monitor;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import net.geant.nmaas.externalservices.api.model.MonitorEntryView;
import net.geant.nmaas.externalservices.monitor.entities.MonitorEntry;
import net.geant.nmaas.externalservices.monitor.exceptions.MonitorEntryNotFound;
import net.geant.nmaas.externalservices.monitor.repositories.MonitorRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MonitorManager {

    private MonitorRepository repository;

    private ModelMapper modelMapper;

    @Autowired
    public MonitorManager(MonitorRepository repository, ModelMapper modelMapper){
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    public void createMonitorEntry(MonitorEntryView monitorEntryView){
        validateMonitorEntryCreation(monitorEntryView);
        this.repository.save(modelMapper.map(monitorEntryView, MonitorEntry.class));
    }

    public void updateMonitorEntry(MonitorEntryView monitorEntryView){
        MonitorEntry monitorEntry = this.repository.findByServiceName(monitorEntryView.getServiceName())
                .orElseThrow(() -> new MonitorEntryNotFound(String.format("Monitor entry for %s cannot be found", monitorEntryView.getServiceName().getName())));
        validateMonitorEntryUpdate(monitorEntryView);
        monitorEntryView.setId(monitorEntry.getId());
        this.repository.save(modelMapper.map(monitorEntryView, MonitorEntry.class));
    }

    public void updateMonitorEntry(Date lastCheck, ServiceType serviceType, MonitorStatus status){
        MonitorEntry monitorEntry = this.repository.findByServiceName(serviceType)
                .orElseThrow(() -> new MonitorEntryNotFound(String.format("Monitor entry for %s cannot be found", serviceType.getName())));
        validateMonitorEntryUpdate(lastCheck, status);
        monitorEntry.setStatus(status);
        monitorEntry.setLastCheck(lastCheck);
        if(status.equals(MonitorStatus.SUCCESS))
            monitorEntry.setLastSuccess(lastCheck);
        this.repository.save(monitorEntry);
    }

    public void deleteMonitorEntry(MonitorEntryView monitorEntryView){
        if(this.repository.existsByServiceName(monitorEntryView.getServiceName()))
            this.repository.deleteByServiceName(monitorEntryView.getServiceName());
    }

    public List<MonitorEntryView> getAllMonitorEntries(){
        return this.repository.findAll().stream()
                .map(entity -> this.modelMapper.map(entity, MonitorEntryView.class))
                .collect(Collectors.toList());
    }

    public MonitorEntryView getMonitorEntries(ServiceType serviceType){
        return this.repository.findByServiceName(serviceType)
                .map(entity -> this.modelMapper.map(entity, MonitorEntryView.class))
                .orElseThrow(() -> new MonitorEntryNotFound(String.format("Monitor entry for %s cannot be found", serviceType.getName())));
    }

    private void validateMonitorEntryUpdate(Date lastCheck, MonitorStatus status){
        if(status == null)
            throw new IllegalStateException("Status cannot be null");
        if(lastCheck == null || lastCheck.after(new Date()))
            throw new IllegalStateException("Last check date cannot be null or from future");
    }

    private void validateMonitorEntryUpdate(MonitorEntryView monitorEntryView){
        if(monitorEntryView.getCheckInterval() == null || monitorEntryView.getCheckInterval() <= 0)
            throw new IllegalStateException("Check interval cannot be less or equal 0");
        if(monitorEntryView.getStatus() == null)
            throw new IllegalStateException("Status cannot be null");
        if(monitorEntryView.getLastCheck() == null || monitorEntryView.getLastCheck().after(new Date()))
            throw new IllegalStateException("Last check date cannot be null or from future");
    }

    private void validateMonitorEntryCreation(MonitorEntryView monitorEntryView){
        if(monitorEntryView.getCheckInterval() == null || monitorEntryView.getCheckInterval() <= 0)
            throw new IllegalStateException("Check interval cannot be less or equal 0");
        if(monitorEntryView.getServiceName() == null || repository.existsByServiceName(monitorEntryView.getServiceName()))
            throw new IllegalStateException("Service name is null or already created");
    }
}
