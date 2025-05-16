package net.geant.nmaas.externalservices.kubernetes.repositories;

import net.geant.nmaas.externalservices.kubernetes.entities.KCluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KClusterRepository extends JpaRepository<KCluster, Long> {
}
