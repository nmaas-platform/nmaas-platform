package net.geant.nmaas.externalservices.inventory.network.repositories;

import net.geant.nmaas.externalservices.inventory.network.entities.DockerHostAttachPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DockerHostAttachPointRepository extends JpaRepository<DockerHostAttachPoint, Long> {

    Optional<DockerHostAttachPoint> findByDockerHostName(String dockerHostName);

}
