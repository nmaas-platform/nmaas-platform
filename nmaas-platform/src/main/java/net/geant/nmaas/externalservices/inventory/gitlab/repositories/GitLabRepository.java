package net.geant.nmaas.externalservices.inventory.gitlab.repositories;

import net.geant.nmaas.externalservices.inventory.gitlab.entities.GitLab;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GitLabRepository extends JpaRepository<GitLab, Long> {

    Optional<GitLab> findById(Long id);

}
