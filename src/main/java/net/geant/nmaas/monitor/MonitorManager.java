package net.geant.nmaas.monitor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.monitor.entities.MonitorEntry;
import net.geant.nmaas.monitor.exceptions.MonitorEntryNotFound;
import net.geant.nmaas.monitor.model.MonitorEntryDto;
import net.geant.nmaas.monitor.repositories.MonitorRepository;
import net.geant.nmaas.notifications.MailAttributes;
import net.geant.nmaas.notifications.NotificationEvent;
import net.geant.nmaas.notifications.templates.MailType;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MonitorManager {

    private final MonitorRepository repository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;

    public void createMonitorEntry(MonitorEntryDto monitorEntryDto) {
        validateMonitorEntryCreation(monitorEntryDto);
        repository.save(modelMapper.map(monitorEntryDto, MonitorEntry.class));
    }

    public void updateMonitorEntry(MonitorEntryDto monitorEntryDto) {
        MonitorEntry monitorEntry = repository.findByServiceName(monitorEntryDto.getServiceName())
                .orElseThrow(() -> new MonitorEntryNotFound(monitorEntryNotFoundMessage(monitorEntryDto.getServiceName().getName())));
        validateMonitorEntryUpdate(monitorEntryDto);
        monitorEntryDto.setId(monitorEntry.getId());
        repository.save(modelMapper.map(monitorEntryDto, MonitorEntry.class));
    }

    public void updateMonitorEntry(Date lastCheck, ServiceType serviceType, MonitorStatus status) {
        MonitorEntry monitorEntry = repository.findByServiceName(serviceType)
                .orElseThrow(() -> new MonitorEntryNotFound(monitorEntryNotFoundMessage(serviceType.getName())));
        validateMonitorEntryUpdate(lastCheck, status);
        monitorEntry.setStatus(status);
        monitorEntry.setLastCheck(lastCheck);
        log.trace("Updating monitor entry: {}, {}, {}", lastCheck, serviceType.toString(), status);
        if (status.equals(MonitorStatus.SUCCESS)) {
            monitorEntry.setLastSuccess(lastCheck);
        } else if (status.equals(MonitorStatus.FAILURE)) {
            eventPublisher.publishEvent(new NotificationEvent(this, getMailAttributes(serviceType.getName())));
        }
        repository.save(monitorEntry);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteMonitorEntry(String serviceName) {
        if (repository.existsByServiceName(ServiceType.valueOf(serviceName.toUpperCase()))) {
            repository.deleteByServiceName(ServiceType.valueOf(serviceName.toUpperCase()));
        }
    }

    public List<MonitorEntryDto> getAllMonitorEntries() {
        return repository.findAll().stream()
                .map(entity -> modelMapper.map(entity, MonitorEntryDto.class))
                .toList();
    }

    public MonitorEntryDto getMonitorEntries(String serviceName) {
        return repository.findByServiceName(ServiceType.valueOf(serviceName.toUpperCase()))
                .map(entity -> modelMapper.map(entity, MonitorEntryDto.class))
                .orElseThrow(() -> new MonitorEntryNotFound(monitorEntryNotFoundMessage(serviceName)));
    }

    public void changeJobState(String serviceName, boolean active) {
        MonitorEntry monitorEntry = repository.findByServiceName(ServiceType.valueOf(serviceName.toUpperCase()))
                .orElseThrow(() -> new MonitorEntryNotFound(monitorEntryNotFoundMessage(serviceName.toUpperCase())));
        monitorEntry.setActive(active);
        repository.save(monitorEntry);
    }

    public boolean existsByServiceName(ServiceType serviceName) {
        return repository.existsByServiceName(serviceName);
    }

    private void validateMonitorEntryUpdate(Date lastCheck, MonitorStatus status) {
        if (status == null) {
            throw new IllegalStateException("Status cannot be null");
        }
        if (lastCheck == null || lastCheck.after(new Date())) {
            throw new IllegalStateException("Last check date cannot be null or from future");
        }
    }

    private void validateMonitorEntryUpdate(MonitorEntryDto monitorEntryDto) {
        if (monitorEntryDto.getServiceName() == null) {
            throw new IllegalStateException("Service name cannot be null");
        }
        if (monitorEntryDto.getCheckInterval() == null || monitorEntryDto.getCheckInterval() <= 0) {
            throw new IllegalStateException("Check interval cannot be less or equal 0");
        }
        if (monitorEntryDto.getTimeFormat() == null) {
            throw new IllegalStateException("Time format cannot be null");
        }
    }

    private void validateMonitorEntryCreation(MonitorEntryDto monitorEntryDto) {
        if (monitorEntryDto.getCheckInterval() == null || monitorEntryDto.getCheckInterval() <= 0) {
            throw new IllegalStateException("Check interval cannot be less or equal 0");
        }
        if (monitorEntryDto.getTimeFormat() == null) {
            throw new IllegalStateException("Time format cannot be null");
        }
        if (monitorEntryDto.getServiceName() == null || repository.existsByServiceName(monitorEntryDto.getServiceName())) {
            throw new IllegalStateException("Service name is null or already created");
        }
    }

    private String monitorEntryNotFoundMessage(String service) {
        return String.format("Monitor entry for %s cannot be found", service);
    }

    private MailAttributes getMailAttributes(String service) {
        return MailAttributes.builder()
                .mailType(MailType.EXTERNAL_SERVICE_HEALTH_CHECK)
                .otherAttributes(Map.of("serviceName", service))
                .build();
    }
}
