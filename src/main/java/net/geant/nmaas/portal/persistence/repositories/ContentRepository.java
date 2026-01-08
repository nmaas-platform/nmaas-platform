package net.geant.nmaas.portal.persistence.repositories;

import net.geant.nmaas.portal.persistence.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContentRepository extends JpaRepository<Content, String> {

    boolean existsByName(String name);

    boolean existsById(Long id);

    Optional<Content> findByName(String name);

    Optional<Content> findById(Long id);
}
