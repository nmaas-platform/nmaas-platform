package net.geant.nmaas.portal.persistent.repositories;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.geant.nmaas.portal.persistent.entity.Domain;

@Repository
public interface DomainRepository extends JpaRepository<Domain, Long> {
	Domain findByName(String name);
}
