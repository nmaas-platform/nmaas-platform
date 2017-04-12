package net.geant.nmaas.orchestration.repositories;

import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppDeploymentRepository extends JpaRepository<AppDeployment, Long> {
    Optional<AppDeployment> findByDeploymentId(Identifier deploymentId);
}
