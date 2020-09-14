package net.geant.nmaas.portal.service;

import java.util.List;
import java.util.Optional;

import net.geant.nmaas.portal.api.domain.ApplicationView;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import net.geant.nmaas.portal.persistent.entity.Application;

public interface ApplicationService {
	
	Application create(ApplicationView request, String owner);
	Application update(Application app);
	void delete(Long id);

	void changeApplicationState(Application app, ApplicationState state);
	
	Optional<Application> findApplication(Long applicationId);
	Application findApplicationLatestVersion(String name);

	Page<Application> findAll(Pageable pageable);
	List<Application> findAll();

	void setMissingProperties(ApplicationView app, Long appId);

	boolean exists(String name, String version);

	Long createOrUpdate(Application application);

}
