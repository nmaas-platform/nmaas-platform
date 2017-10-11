package net.geant.nmaas.nmservice.deployment.repository;

import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Repository
public interface NmServiceInfoRepository extends JpaRepository<NmServiceInfo, Long>  {

    Optional<NmServiceInfo> findByDeploymentId(Identifier deploymentId);

    List<NmServiceInfo> findAllByClientId(Identifier clientId);

    @Query("SELECT n.state FROM NmServiceInfo n WHERE n.deploymentId = :deploymentId")
    Optional<NmServiceDeploymentState> getStateByDeploymentId(@Param("deploymentId") Identifier deploymentId);

    @Query("SELECT n.dockerContainer.id FROM NmServiceInfo n WHERE n.deploymentId = :deploymentId")
    Optional<String> getContainerIdByDeploymentId(@Param("deploymentId") Identifier deploymentId);

    @Query("SELECT n.clientId FROM NmServiceInfo n WHERE n.deploymentId = :deploymentId")
    Optional<Identifier> getClientIdByDeploymentId(@Param("deploymentId") Identifier deploymentId);

    @Query("SELECT n.applicationId FROM NmServiceInfo n WHERE n.deploymentId = :deploymentId")
    Optional<Identifier> getApplicationIdByDeploymentId(@Param("deploymentId") Identifier deploymentId);

}
