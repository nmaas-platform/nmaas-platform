package net.geant.nmaas.portal.persistence.repositories;

import net.geant.nmaas.portal.persistence.entity.InternationalizationSimple;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InternationalizationSimpleRepository extends JpaRepository<InternationalizationSimple, String> {
    Optional<InternationalizationSimple> findByLanguageOrderByIdDesc(String language);
}
