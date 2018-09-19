package net.geant.nmaas.externalservices.inventory.network.repositories;

import net.geant.nmaas.externalservices.inventory.network.entities.DomainNetworkAttachPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DomainNetworkAttachPointRepository extends JpaRepository<DomainNetworkAttachPoint, Long> {

    Optional<DomainNetworkAttachPoint> findByDomain(String domain);

}
