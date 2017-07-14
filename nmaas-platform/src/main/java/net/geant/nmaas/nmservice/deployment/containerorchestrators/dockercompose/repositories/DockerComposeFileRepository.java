package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.repositories;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeFile;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Repository
public interface DockerComposeFileRepository extends JpaRepository<DockerComposeFile, Long> {

    Optional<DockerComposeFile> findByDeploymentId(Identifier deploymentId);

}
