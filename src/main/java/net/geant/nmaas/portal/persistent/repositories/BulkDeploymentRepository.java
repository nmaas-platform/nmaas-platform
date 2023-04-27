package net.geant.nmaas.portal.persistent.repositories;

import net.geant.nmaas.portal.persistent.entity.BulkDeployment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BulkDeploymentRepository extends JpaRepository<BulkDeployment, Long> {
}
