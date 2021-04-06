package net.geant.nmaas.orchestration.repositories;

import net.geant.nmaas.orchestration.entities.AppTermsAcceptance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppTermsAcceptanceRepository extends JpaRepository<AppTermsAcceptance, Long> {
}
