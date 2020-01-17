package net.geant.nmaas.portal.persistent.repositories;

import net.geant.nmaas.portal.persistent.entity.Internationalization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/*
old internationalization repository to be removed
 */
@Repository
public interface InternationalizationRepository extends JpaRepository<Internationalization, String> {
    Optional<Internationalization> findByLanguageOrderByIdDesc(String language);
}
