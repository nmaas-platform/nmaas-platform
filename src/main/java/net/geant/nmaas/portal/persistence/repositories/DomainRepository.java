package net.geant.nmaas.portal.persistence.repositories;

import net.geant.nmaas.portal.persistence.entity.Domain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DomainRepository extends JpaRepository<Domain, Long>, JpaSpecificationExecutor<Domain> {

    boolean existsByName(String name);

    Optional<Domain> findByName(String name);

    boolean existsByCodename(String name);

    Optional<Domain> findByCodename(String name);

    long countByActiveTrueAndDeletedFalse();

}
