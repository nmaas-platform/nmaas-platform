package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.repositories;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerNetwork;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Repository
public interface DockerNetworkRepository extends JpaRepository<DockerNetwork, Long> {

    Optional<DockerNetwork> findByClientId(Identifier clientId);

}
