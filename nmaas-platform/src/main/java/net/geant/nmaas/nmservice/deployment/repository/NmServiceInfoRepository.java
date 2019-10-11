package net.geant.nmaas.nmservice.deployment.repository;

import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.orchestration.Identifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NmServiceInfoRepository<T extends NmServiceInfo> extends JpaRepository<T, Long>  {

    @Query("select t from #{#entityName} t where t.domain = ?1")
    List<T> findAllByDomain(String domain);

    @Query("select t from #{#entityName} t where t.deploymentId = ?1")
    Optional<T> findByDeploymentId(Identifier deploymentId);

    @Query("SELECT n.state FROM #{#entityName} n WHERE n.deploymentId = :deploymentId")
    Optional<NmServiceDeploymentState> getStateByDeploymentId(@Param("deploymentId") Identifier deploymentId);

    @Query("SELECT n.domain FROM #{#entityName} n WHERE n.deploymentId = :deploymentId")
    Optional<String> getDomainByDeploymentId(@Param("deploymentId") Identifier deploymentId);

    @Query("SELECT n.deploymentName FROM #{#entityName} n WHERE n.deploymentId = :deploymentId")
    Optional<String> getDeploymentNameByDeploymentId(@Param("deploymentId") Identifier deploymentId);

    @Query("SELECT n.descriptiveDeploymentId FROM #{#entityName} n WHERE n.deploymentId = :deploymentId")
    Optional<Identifier> getDescriptiveDeploymentIdByDeploymentId(@Param("deploymentId") Identifier deploymentId);

}
