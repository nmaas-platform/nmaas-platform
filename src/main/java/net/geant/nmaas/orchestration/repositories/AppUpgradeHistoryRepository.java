package net.geant.nmaas.orchestration.repositories;

import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppUpgradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface AppUpgradeHistoryRepository extends JpaRepository<AppUpgradeHistory, Long> {

    List<AppUpgradeHistory> findByDeploymentId(Identifier deploymentId);

    @Query("SELECT h FROM AppUpgradeHistory h WHERE h.timestamp >= :from AND h.timestamp < :to")
    List<AppUpgradeHistory> findInPeriod(@Param("from") Date from, @Param("to") Date to);

}
