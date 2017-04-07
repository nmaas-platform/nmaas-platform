package net.geant.nmaas.orchestration.repositories;

import net.geant.nmaas.orchestration.entities.AppDeployment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppDeploymentRepository extends JpaRepository<AppDeployment, Long> {
}
