package net.geant.nmaas.externalservices.inventory.dockerhosts.repositories;

import net.geant.nmaas.externalservices.inventory.dockerhosts.entities.DockerHostState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DockerHostStateRepository extends JpaRepository<DockerHostState, Long>  {

    Optional<DockerHostState> findByDockerHostName(String name);

}
