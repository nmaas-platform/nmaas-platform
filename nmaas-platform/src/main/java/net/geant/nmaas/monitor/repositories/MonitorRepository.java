package net.geant.nmaas.monitor.repositories;

import java.util.Optional;
import net.geant.nmaas.monitor.ServiceType;
import net.geant.nmaas.monitor.entities.MonitorEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonitorRepository extends JpaRepository <MonitorEntry, Long> {
    Optional<MonitorEntry> findByServiceName(ServiceType serviceName);
    boolean existsByServiceName(ServiceType serviceName);
    void deleteByServiceName(ServiceType serviceName);
}
