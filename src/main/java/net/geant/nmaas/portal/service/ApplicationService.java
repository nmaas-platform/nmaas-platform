package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ApplicationService {

	Application create(Application application);
	Application update(Application application);
	void delete(Long id);

	void changeApplicationState(Application app, ApplicationState state);
	
	Optional<Application> findApplication(Long id);
	Optional<Application> findApplication(String name, String version);
	Application findApplicationLatestVersion(String name);

	/**
	 * Retrieves all Helm chart versions of given application with corresponding application version
	 *
	 * @param name Application name
	 * @return map of application Helm chart version and corresponding application version
	 */
	Map<String, String> findAllVersionNumbers(String name);

	Page<Application> findAll(Pageable pageable);
	List<Application> findAll();

	void setMissingProperties(Application app, Long appId);

	boolean exists(String name, String version);

}
