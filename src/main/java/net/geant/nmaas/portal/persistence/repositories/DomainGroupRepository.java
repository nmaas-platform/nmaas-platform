package net.geant.nmaas.portal.persistence.repositories;

import net.geant.nmaas.portal.persistence.entity.DomainGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DomainGroupRepository extends JpaRepository<DomainGroup, Long> {

    boolean existsByName(String name);

    boolean existsByCodename(String codename);

    Optional<DomainGroup> findByCodename(String codeName);
}
