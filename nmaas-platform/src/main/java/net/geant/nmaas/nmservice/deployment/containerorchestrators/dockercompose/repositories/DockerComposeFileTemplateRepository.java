package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.repositories;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeFileTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Repository
public interface DockerComposeFileTemplateRepository extends JpaRepository<DockerComposeFileTemplate, Long> {

}
