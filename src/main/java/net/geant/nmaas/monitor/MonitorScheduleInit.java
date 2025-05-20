package net.geant.nmaas.monitor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.monitor.model.MonitorEntryView;
import net.geant.nmaas.scheduling.ScheduleManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MonitorScheduleInit implements InitializingBean {

    private final List<MonitorService> monitorServices;
    private final ScheduleManager scheduleManager;
    private final MonitorManager monitorManager;

    @Override
    @Transactional
    public void afterPropertiesSet() {
        Arrays.stream(ServiceType.values())
                .filter(serviceType -> !scheduleManager.jobExists(serviceType.toString())) // if job does not exist
                .forEach(serviceType -> {
                    MonitorEntryView monitorEntry;
                    if (monitorManager.existsByServiceName(serviceType)) { // if entry exists
                        monitorEntry = monitorManager.getMonitorEntries(serviceType.toString()); // read it from database
                    } else {
                        monitorEntry = serviceType.getDefaultMonitorEntry(); // if entry does not exist
                        monitorManager.createMonitorEntry(monitorEntry); // create new default entry
                    }
                    Optional<MonitorService> service = monitorServices.stream()
                            .filter(s -> s.getServiceType().equals(serviceType))
                            .filter(MonitorService::schedulable)
                            .findFirst();
                    if (service.isPresent()) {
                        scheduleManager.createJob(service.get(), monitorEntry);
                    } else {
                        log.warn("Monitor service for {} not found or is not schedulable", serviceType);
                    }
                });
    }

}