package net.geant.nmaas.externalservices.inventory.kubernetes.repositories;

import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KubernetesCluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Repository
public interface KubernetesClusterRepository extends JpaRepository<KubernetesCluster, Long> {

    Optional<KubernetesCluster> findByName(String name);

}
