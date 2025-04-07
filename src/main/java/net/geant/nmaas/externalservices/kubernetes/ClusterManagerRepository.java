package net.geant.nmaas.externalservices.kubernetes;

import net.geant.nmaas.externalservices.kubernetes.model.ClusterManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClusterManagerRepository extends JpaRepository<ClusterManager, Long> {
}
