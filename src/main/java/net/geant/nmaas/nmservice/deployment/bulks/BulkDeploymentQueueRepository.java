package net.geant.nmaas.nmservice.deployment.bulks;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BulkDeploymentQueueRepository extends JpaRepository<BulkDeploymentQueueEntry, Long> {

    Optional<BulkDeploymentQueueEntry> findByBulkEntryId(Long bulkId);

}