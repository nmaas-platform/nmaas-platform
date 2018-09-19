package net.geant.nmaas.externalservices.inventory.shibboleth.repositories;

import net.geant.nmaas.externalservices.inventory.shibboleth.entities.Shibboleth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShibbolethRepository extends JpaRepository<Shibboleth, Long> { }
