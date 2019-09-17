package net.geant.nmaas.portal.persistent.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.Tag;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

	List<Application> findByName(String name);
	
	List<Application> findByTags(Tag tag);

	boolean existsByNameAndVersion(String name, String version);
}
