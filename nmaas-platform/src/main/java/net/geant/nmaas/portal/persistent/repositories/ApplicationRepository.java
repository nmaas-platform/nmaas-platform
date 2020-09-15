package net.geant.nmaas.portal.persistent.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.geant.nmaas.portal.persistent.entity.Application;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

	List<Application> findByName(String name);

	boolean existsByNameAndVersion(String name, String version);

	Optional<Application> findByNameAndVersion(String name, String version);
}
