package net.geant.nmaas.monitor;

import net.geant.nmaas.monitor.entities.MonitorEntry;
import net.geant.nmaas.monitor.exceptions.MonitorEntryNotFound;
import net.geant.nmaas.monitor.model.MonitorEntryView;
import net.geant.nmaas.monitor.repositories.MonitorRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
                .orElseThrow(() -> new MonitorEntryNotFound(monitorEntryNotFoundMessage(monitorEntryView.getServiceName().getName())));
        validateMonitorEntryUpdate(monitorEntryView);
        monitorEntryView.setId(monitorEntry.getId());
        this.repository.save(modelMapper.map(monitorEntryView, MonitorEntry.class));
    }

    public void updateMonitorEntry(Date lastCheck, ServiceType serviceType, MonitorStatus status){
        MonitorEntry monitorEntry = this.repository.findByServiceName(serviceType)
                .orElseThrow(() -> new MonitorEntryNotFound(monitorEntryNotFoundMessage(serviceType.getName())));
        validateMonitorEntryUpdate(lastCheck, status);
        monitorEntry.setStatus(status);
        monitorEntry.setLastCheck(lastCheck);
        if(status.equals(MonitorStatus.SUCCESS))
            monitorEntry.setLastSuccess(lastCheck);
        this.repository.save(monitorEntry);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteMonitorEntry(String serviceName){
        if(this.repository.existsByServiceName(ServiceType.valueOf(serviceName.toUpperCase())))
            this.repository.deleteByServiceName(ServiceType.valueOf(serviceName.toUpperCase()));
    }

    public List<MonitorEntryView> getAllMonitorEntries(){
        return this.repository.findAll().stream()
                .map(entity -> this.modelMapper.map(entity, MonitorEntryView.class))
                .collect(Collectors.toList());
    }

    public MonitorEntryView getMonitorEntries(String serviceName){
        return this.repository.findByServiceName(ServiceType.valueOf(serviceName.toUpperCase()))
                .map(entity -> this.modelMapper.map(entity, MonitorEntryView.class))
                .orElseThrow(() -> new MonitorEntryNotFound(monitorEntryNotFoundMessage(serviceName)));
    }

    private void validateMonitorEntryUpdate(Date lastCheck, MonitorStatus status){
        if(status == null)
            throw new IllegalStateException("Status cannot be null");
        if(lastCheck == null || lastCheck.after(new Date()))
            throw new IllegalStateException("Last check date cannot be null or from future");
    }

    private void validateMonitorEntryUpdate(MonitorEntryView monitorEntryView){
        if(monitorEntryView.getServiceName() == null)
            throw new IllegalStateException("Service name cannot be null");
        if(monitorEntryView.getCheckInterval() == null || monitorEntryView.getCheckInterval() <= 0)
            throw new IllegalStateException("Check interval cannot be less or equal 0");
        if(monitorEntryView.getTimeFormat() == null)
            throw new IllegalStateException("Time format cannot be null");
    }

    private void validateMonitorEntryCreation(MonitorEntryView monitorEntryView){
        if(monitorEntryView.getCheckInterval() == null || monitorEntryView.getCheckInterval() <= 0)
            throw new IllegalStateException("Check interval cannot be less or equal 0");
        if(monitorEntryView.getTimeFormat() == null)
            throw new IllegalStateException("Time format cannot be null");
        if(monitorEntryView.getServiceName() == null || repository.existsByServiceName(monitorEntryView.getServiceName()))
            throw new IllegalStateException("Service name is null or already created");
    }

    private String monitorEntryNotFoundMessage(String service) {
        return String.format("Monitor entry for %s cannot be found", service);
    }
}
