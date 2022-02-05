package net.geant.nmaas.monitor;

import net.geant.nmaas.monitor.exceptions.MonitorServiceNotFound;
import net.geant.nmaas.monitor.model.MonitorEntryView;
import net.geant.nmaas.scheduling.ScheduleManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Configuration
public class MonitorConfig {

    @Bean
    public InitializingBean insertDefaultMonitoringJobs(){
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
                        .filter(serviceType -> !this.scheduleManager.jobExists(serviceType.toString())) // if job does not exist
                        .forEach(serviceType -> {
                            MonitorEntryView monitorEntry;
                            if (monitorManager.existsByServiceName(serviceType)) { // if entry exists
                                monitorEntry = monitorManager.getMonitorEntries(serviceType.toString()); // read it from database
                            } else {
                                monitorEntry = serviceType.getDefaultMonitorEntry(); // if entry does not exist
                                this.monitorManager.createMonitorEntry(monitorEntry); // create new default entry
                            }
                            MonitorService service = monitorServices.stream()
                                    .filter(s->s.getServiceType().equals(serviceType))
                                    .findAny()
                                    .orElseThrow(() -> new MonitorServiceNotFound(String.format("Monitor service for %s not found", serviceType)));
                            this.scheduleManager.createJob(service, monitorEntry);
                        });
            }
        };
    }

}
