package net.geant.nmaas.portal.persistence.repositories;

import net.geant.nmaas.api.dto.domains.DomainBaseDto;
import net.geant.nmaas.portal.persistence.entity.Domain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DomainRepository extends JpaRepository<Domain, Long>, JpaSpecificationExecutor<Domain> {

    boolean existsByName(String name);

    Optional<Domain> findByName(String name);

    boolean existsByCodename(String name);

    Optional<Domain> findByCodename(String name);

    @Query("SELECT new net.geant.nmaas.api.dto.domains.DomainBaseDto(d.id, d.name, d.codename, d.active, d.deleted, null) FROM Domain d where d.deleted = false")
    List<DomainBaseDto> findAllBaseDomains();

    @Query("SELECT new net.geant.nmaas.api.dto.domains.DomainBaseDto(d.id, d.name, d.codename, d.active, d.deleted, null) FROM Domain d where d.deleted = false")
    Page<DomainBaseDto> findAllBaseDomainsPageable(Pageable pageable);

    Page<Domain> findAll(Specification<Domain> spec, Pageable pageable);

    List<Domain> findAll(Specification<Domain> spec);

    long countByActiveTrueAndDeletedFalse();

}
