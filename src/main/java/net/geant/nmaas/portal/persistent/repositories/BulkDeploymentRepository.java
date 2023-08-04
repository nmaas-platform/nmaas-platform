package net.geant.nmaas.portal.persistent.repositories;

import net.geant.nmaas.portal.api.bulk.BulkType;
import net.geant.nmaas.portal.persistent.entity.BulkDeployment;
import net.geant.nmaas.portal.persistent.entity.BulkDeploymentState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BulkDeploymentRepository extends JpaRepository<BulkDeployment, Long> {

    List<BulkDeployment> findByType(BulkType bulkType);

    List<BulkDeployment> findByTypeAndState(BulkType bulkType, BulkDeploymentState bulkDeploymentState);

}
