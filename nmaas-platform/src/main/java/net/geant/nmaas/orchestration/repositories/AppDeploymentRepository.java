package net.geant.nmaas.orchestration.repositories;

import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppDeploymentRepository extends JpaRepository<AppDeployment, Long> {

    Optional<AppDeployment> findByDeploymentId(Identifier deploymentId);

    @Query("SELECT a.state FROM AppDeployment a WHERE a.deploymentId = :deploymentId")
    Optional<AppDeploymentState> getStateByDeploymentId(@Param("deploymentId") Identifier deploymentId);

    @Query("SELECT a.clientId FROM AppDeployment a WHERE a.deploymentId = :deploymentId")
    Optional<Identifier> getClientIdByDeploymentId(@Param("deploymentId") Identifier deploymentId);

}
