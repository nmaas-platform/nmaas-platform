package net.geant.nmaas.portal.persistent.repositories;

import java.util.Optional;

import net.geant.nmaas.portal.persistent.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentRepository extends JpaRepository<Content, String> {

    boolean existsByName(String name);
    boolean existsById(Long id);
    Optional<Content> findByName(String name);
    Optional<Content> findById(Long id);
}
