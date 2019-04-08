package net.geant.nmaas.dcn.deployment.repositories;

import java.util.Optional;
import net.geant.nmaas.dcn.deployment.entities.DomainDcnDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DomainDcnDetailsRepository extends JpaRepository<DomainDcnDetails, Long> {
    Optional<DomainDcnDetails> findByDomainCodename(String domainCodename);
}
