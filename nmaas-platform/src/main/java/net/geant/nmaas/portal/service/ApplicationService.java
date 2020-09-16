package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ApplicationService {

	Application create(Application application);

	Application create(Application application);
	Application update(Application application);
	void delete(Long id);

	void changeApplicationState(Application app, ApplicationState state);
	
	Optional<Application> findApplication(Long id);
	Optional<Application> findApplication(String name, String version);
	Application findApplicationLatestVersion(String name);

	Page<Application> findAll(Pageable pageable);
	List<Application> findAll();

	void setMissingProperties(Application app, Long appId);
	void setMissingProperties(Application app, Long appId);

	boolean exists(String name, String version);

}
