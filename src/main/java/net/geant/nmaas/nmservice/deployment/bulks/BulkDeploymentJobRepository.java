package net.geant.nmaas.nmservice.deployment.bulks;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BulkDeploymentJobRepository extends JpaRepository<BulkDeploymentJobEntry, Long> {
}
