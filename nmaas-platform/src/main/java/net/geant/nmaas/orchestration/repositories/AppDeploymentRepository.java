package net.geant.nmaas.orchestration.repositories;

import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.projections.AppDeploymentCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppDeploymentRepository extends JpaRepository<AppDeployment, Long> {

    Optional<AppDeployment> findByDeploymentId(Identifier deploymentId);

    List<AppDeployment> findByDomainAndState(String domain, AppDeploymentState state);

    Optional<AppDeployment> findByDeploymentNameAndDomain(String deploymentName, String domain);

    @Query("SELECT a.state FROM AppDeployment a WHERE a.deploymentId = :deploymentId")
    Optional<AppDeploymentState> getStateByDeploymentId(@Param("deploymentId") Identifier deploymentId);

    @Query("SELECT a.domain FROM AppDeployment a WHERE a.deploymentId = :deploymentId")
    Optional<String> getDomainByDeploymentId(@Param("deploymentId") Identifier deploymentId);

    @Query("SELECT a.errorMessage FROM AppDeployment a WHERE a.deploymentId = :deploymentId")
    Optional<String> getErrorMessageByDeploymentId(@Param("deploymentId") Identifier deploymentId);

    @Query("select d.name FROM AppDeployment a join Domain d on a.domain = d.codename where a.deploymentId = :deploymentId")
    Optional<String> getDomainNameByDeploymentId(@Param("deploymentId") Identifier deploymentId);

    @Query("SELECT d.appName AS applicationName, COUNT(d.appName) AS count FROM AppDeployment AS d WHERE d.state = 'APPLICATION_DEPLOYMENT_VERIFIED' GROUP BY d.appName")
    List<AppDeploymentCount> countAllRunningByAppName();
}
