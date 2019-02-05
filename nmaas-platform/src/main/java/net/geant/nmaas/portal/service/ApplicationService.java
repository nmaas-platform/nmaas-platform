package net.geant.nmaas.portal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import net.geant.nmaas.portal.persistent.entity.Application;

public interface ApplicationService {
	
	Application create(String name);
	Application update(Application app);
	void delete(Long id);
	
	Optional<Application> findApplication(Long applicationId);

	Page<Application> findAll(Pageable pageable);
	List<Application> findAll();

	
}
