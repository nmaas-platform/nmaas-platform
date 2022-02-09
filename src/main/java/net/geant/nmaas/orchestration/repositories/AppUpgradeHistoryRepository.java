package net.geant.nmaas.orchestration.repositories;

import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppUpgradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppUpgradeHistoryRepository extends JpaRepository<AppUpgradeHistory, Long> {

    List<AppUpgradeHistory> findByDeploymentId(Identifier deploymentId);

}
