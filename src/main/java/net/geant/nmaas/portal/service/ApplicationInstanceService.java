package net.geant.nmaas.portal.service;

import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.api.model.AppConfigurationView;
import net.geant.nmaas.portal.api.domain.AppInstanceView;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ApplicationInstanceService {

	AppInstance create(Long domainId, Long applicationId, String name, boolean autoUpgradesEnabled);
	AppInstance create(Domain domain, Application application, String name, boolean autoUpgradesEnabled);
	
	void delete(Long appInstanceId);

	void update(AppInstance appInstance);
	void updateApplication(AppInstance appInstance, Application application);
	
	Optional<AppInstance> find(Long appInstanceId);
    Optional<AppInstance> findByInternalId(Identifier deploymentId);

	List<AppInstance> findAll();
	Page<AppInstance> findAll(Pageable pageable);

	List<AppInstance> findAllByOwner(Long userId);
	List<AppInstance> findAllByOwner(User owner);
	List<AppInstance> findAllByOwner(Long userId, Long domainId);
	List<AppInstance> findAllByOwnerAndDomain(User owner, Domain domain);

	Page<AppInstance> findAllByOwner(Long userId, Pageable pageable);
	Page<AppInstance> findAllByOwner(User owner, Pageable pageable);
	Page<AppInstance> findAllByOwner(Long userId, Long domainId, Pageable pageable);
	Page<AppInstance> findAllByOwner(User owner, Domain domain, Pageable pageable);

	List<AppInstance> findAllByDomain(Long domainId);
	List<AppInstance> findAllByDomain(Domain domain);
	Page<AppInstance> findAllByDomain(Long domainId, Pageable pageable);
	Page<AppInstance> findAllByDomain(Domain domain, Pageable pageable);

	boolean validateAgainstAppConfiguration(AppInstance appInstance, AppConfigurationView appConfigurationView);

	boolean checkUpgradePossible(Long appInstanceId);
    AppInstanceView.AppInstanceUpgradeInfo obtainUpgradeInfo(Long appInstanceId);

}
