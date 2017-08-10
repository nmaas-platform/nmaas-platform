package net.geant.nmaas.nmservice.deployment.repository;

import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Repository
public interface NmServiceInfoRepository<T extends NmServiceInfo> extends JpaRepository<T, Long>  {

    @Query("select t from #{#entityName} t where t.deploymentId = ?1")
    Optional<T> findByDeploymentId(Identifier deploymentId);

    @Query("SELECT n.state FROM #{#entityName} n WHERE n.deploymentId = :deploymentId")
    Optional<NmServiceDeploymentState> getStateByDeploymentId(@Param("deploymentId") Identifier deploymentId);

    @Query("SELECT n.clientId FROM #{#entityName} n WHERE n.deploymentId = :deploymentId")
    Optional<Identifier> getClientIdByDeploymentId(@Param("deploymentId") Identifier deploymentId);

    @Query("SELECT n.applicationId FROM #{#entityName} n WHERE n.deploymentId = :deploymentId")
    Optional<Identifier> getApplicationIdByDeploymentId(@Param("deploymentId") Identifier deploymentId);

}
