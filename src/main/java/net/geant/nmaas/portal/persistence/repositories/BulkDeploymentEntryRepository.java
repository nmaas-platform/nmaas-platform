package net.geant.nmaas.portal.persistence.repositories;

import net.geant.nmaas.portal.persistence.entity.BulkDeploymentEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BulkDeploymentEntryRepository extends JpaRepository<BulkDeploymentEntry, Long> {
}
