package net.geant.nmaas.externalservices.inventory.shibboleth.repositories;

import net.geant.nmaas.externalservices.inventory.shibboleth.entities.Shibboleth;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShibbolethRepository extends JpaRepository<Shibboleth, Long> { }
