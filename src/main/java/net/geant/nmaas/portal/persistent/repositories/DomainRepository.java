package net.geant.nmaas.portal.persistent.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.geant.nmaas.portal.persistent.entity.Domain;

@Repository
public interface DomainRepository extends JpaRepository<Domain, Long> {
	
	boolean existsByName(String name);
	Optional<Domain> findByName(String name);
	
	boolean existsByCodename(String name);
	Optional<Domain> findByCodename(String name);
}
