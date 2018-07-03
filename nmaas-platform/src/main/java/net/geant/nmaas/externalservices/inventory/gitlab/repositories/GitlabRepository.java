package net.geant.nmaas.externalservices.inventory.gitlab.repositories;

import net.geant.nmaas.externalservices.inventory.gitlab.entities.Gitlab;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GitlabRepository extends JpaRepository<Gitlab, Long> {
    Optional<Gitlab> findById(Long id);
}
