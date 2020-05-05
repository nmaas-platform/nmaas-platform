package net.geant.nmaas.nmservice.configuration.repositories;

import net.geant.nmaas.nmservice.configuration.entities.GitLabProject;
import net.geant.nmaas.orchestration.Identifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GitLabProjectRepository extends JpaRepository<GitLabProject, Long> {

    Optional<GitLabProject> findByDeploymentId(Identifier deploymentId);

}
