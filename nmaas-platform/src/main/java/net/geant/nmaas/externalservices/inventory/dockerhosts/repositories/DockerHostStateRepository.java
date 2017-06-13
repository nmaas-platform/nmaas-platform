package net.geant.nmaas.externalservices.inventory.dockerhosts.repositories;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Repository
public interface DockerHostStateRepository extends JpaRepository<DockerHostState, Long>  {

}
