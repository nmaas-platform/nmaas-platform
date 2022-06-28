package net.geant.nmaas.monitor;

import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.monitor.model.MonitorEntryView;
import net.geant.nmaas.scheduling.ScheduleManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Configuration
@Log4j2
public class MonitorConfig {

    @Bean
    public InitializingBean insertDefaultMonitoringJobs() {
        return new InitializingBean() {

            @Autowired
            private List<MonitorService> monitorServices;

            @Autowired
            private ScheduleManager scheduleManager;

            @Autowired
            private MonitorManager monitorManager;

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
                                log.warn(String.format("Monitor service for %s not found or is not schedulable", serviceType));
                            }
                        });
            }
        };
    }

}
