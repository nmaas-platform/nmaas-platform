package net.geant.nmaas.kubernetes.remote.repositories;

import net.geant.nmaas.kubernetes.remote.entities.KCluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KClusterRepository extends JpaRepository<KCluster, Long> {

    @Query(value = "SELECT kc.* FROM k_cluster kc JOIN k_clusters_domains kcd ON kc.id = kcd.k_cluster_id WHERE kcd.domain_id = :domainId", nativeQuery = true)
    List<KCluster> findByDomains_Id(@Param("domainId") Long domainId);
}
