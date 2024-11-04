package net.geant.nmaas.portal.persistent.repositories;

import net.geant.nmaas.portal.api.bulk.BulkType;
import net.geant.nmaas.portal.persistent.entity.BulkDeployment;
import net.geant.nmaas.portal.persistent.entity.BulkDeploymentState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BulkDeploymentRepository extends JpaRepository<BulkDeployment, Long> {

    List<BulkDeployment> findByType(BulkType bulkType);

    List<BulkDeployment> findByTypeAndState(BulkType bulkType, BulkDeploymentState bulkDeploymentState);

    @Query("select b from BulkDeployment b join b.entries e WHERE e.id  = :entryId")
    BulkDeployment findByBulkEntryId(@Param("entryId") Long entryId);

}
