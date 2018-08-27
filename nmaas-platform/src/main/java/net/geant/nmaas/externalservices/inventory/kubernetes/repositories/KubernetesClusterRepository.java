package net.geant.nmaas.externalservices.inventory.kubernetes.repositories;

import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KCluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Repository
public interface KubernetesClusterRepository extends JpaRepository<KCluster, Long> {
    Optional<KCluster> findById(Long id);
}
