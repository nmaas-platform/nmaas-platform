package net.geant.nmaas.externalservices.inventory.dockerhosts;

import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Repository
public interface DockerHostRepository extends JpaRepository<DockerHost, Long> {

    Optional<DockerHost> findByName(String name);

    Iterable<DockerHost> findByPreferredTrue();

}
