package net.geant.nmaas.externalservices.monitor.repositories;

import java.util.Optional;
import net.geant.nmaas.externalservices.monitor.ServiceType;
import net.geant.nmaas.externalservices.monitor.entities.MonitorEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonitorRepository extends JpaRepository <MonitorEntry, Long> {
    Optional<MonitorEntry> findByServiceName(ServiceType serviceName);
    boolean existsByServiceName(ServiceType serviceName);
    void deleteByServiceName(ServiceType serviceName);
}
