package net.geant.nmaas.orchestration.repositories;

import java.util.Optional;
import net.geant.nmaas.orchestration.entities.DomainTechDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DomainTechDetailsRepository extends JpaRepository<DomainTechDetails, Long> {
    Optional<DomainTechDetails> findByDomainCodename(String domainCodename);
    boolean existsByExternalServiceDomain(String externalServiceDomain);
}
