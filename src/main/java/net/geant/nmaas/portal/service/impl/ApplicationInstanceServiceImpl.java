package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.api.model.AppConfigurationView;
import net.geant.nmaas.orchestration.exceptions.InvalidApplicationIdException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.api.domain.AppInstanceView;
import net.geant.nmaas.portal.exceptions.ApplicationSubscriptionNotActiveException;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationStatePerDomain;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.ApplicationInstanceUpgradeService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.ApplicationStatePerDomainService;
import net.geant.nmaas.portal.service.ApplicationSubscriptionService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

@Service
public class ApplicationInstanceServiceImpl implements ApplicationInstanceService {

	private final AppInstanceRepository appInstanceRepo;
	private final ApplicationService applications;
	private final DomainService domains;
	private final UserService users;
	private final ApplicationSubscriptionService applicationSubscriptions;
	private final DomainServiceImpl.CodenameValidator validator;
	private final ApplicationStatePerDomainService applicationStatePerDomainService;
	private final ApplicationInstanceUpgradeService instanceUpgradeService;

	@Autowired
	public ApplicationInstanceServiceImpl(AppInstanceRepository appInstanceRepo,
                                          ApplicationService applications,
                                          DomainService domains,
                                          UserService users,
                                          ApplicationSubscriptionService applicationSubscriptions,
                                          @Qualifier("InstanceNameValidator") DomainServiceImpl.CodenameValidator validator,
                                          ApplicationStatePerDomainService applicationStatePerDomainService,
										  ApplicationInstanceUpgradeService instanceUpgradeService) {
		this.appInstanceRepo = appInstanceRepo;
		this.applications = applications;
		this.domains = domains;
		this.users = users;
		this.applicationSubscriptions = applicationSubscriptions;
		this.validator = validator;
		this.applicationStatePerDomainService = applicationStatePerDomainService;
		this.instanceUpgradeService = instanceUpgradeService;
	}

	@Override
	public AppInstance create(Long domainId, Long applicationId, String name, boolean autoUpgradesEnabled) {
		Application app = applications.findApplication(applicationId).orElseThrow(() -> new ObjectNotFoundException("Application not found."));
		Domain domain = domains.findDomain(domainId).orElseThrow(() -> new ObjectNotFoundException("Domain not found."));
		return create(domain, app, name, autoUpgradesEnabled);
	}

	@Override
	public AppInstance create(Domain domain, Application application, String name, boolean autoUpgradesEnabled) {
		checkParam(domain);
		if (!domain.isActive()) {
			throw new IllegalArgumentException("Domain is inactive");
		}
		checkParam(application);
		checkNameCharacters(name);

		if (!this.applicationStatePerDomainService.isApplicationEnabledInDomain(domain, application)) {
            throw new IllegalArgumentException("Application is disabled in domain settings");
        }

		if (applicationSubscriptions.isActive(application.getName(), domain)) {
			return appInstanceRepo.save(new AppInstance(application, domain, name, autoUpgradesEnabled));
		} else {
			throw new ApplicationSubscriptionNotActiveException("Application subscription is missing or not active.");
		}
	}

    @Override
    public boolean validateAgainstAppConfiguration(AppInstance appInstance, AppConfigurationView appConfigurationView) {
        Domain domain = appInstance.getDomain();
        Application app = appInstance.getApplication();

        ApplicationStatePerDomain appStatePerDomain = domain.getApplicationStatePerDomain().stream()
				.filter(appState -> appState.getApplicationBase().getName().equals(app.getName()))
				.findAny()
				.orElseThrow(() -> new IllegalArgumentException("Application state not found"));

        return this.applicationStatePerDomainService.validateAppConfigurationAgainstState(appConfigurationView, appStatePerDomain);
    }

    @Override
	public void delete(Long appInstanceId) {
		checkParam(appInstanceId);
		find(appInstanceId).ifPresent(appInstanceRepo::delete);
	}
	
	@Override
	public void update(AppInstance appInstance) {
		checkParam(appInstance);
		appInstanceRepo.save(appInstance);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void updateApplication(Identifier internalId, Long applicationId) {
		checkParam(internalId);
		checkParam(applicationId);
		final AppInstance instance = findByInternalId(internalId)
				.orElseThrow(() -> new InvalidDeploymentIdException("Application instance with internalId " + internalId + " does not exist"));
		final Application application = applications.findApplication(applicationId)
				.orElseThrow(() -> new InvalidApplicationIdException("Application with id " + applicationId + " does not exist"));
		appInstanceRepo.updateApplication(instance.getId(), instance.getApplication().getId(), application);
	}

	@Override
	@Loggable(LogLevel.DEBUG)
	public Optional<AppInstance> findByInternalId(Identifier deploymentId) {
		checkParam(deploymentId);
		return appInstanceRepo.findByInternalId(deploymentId);
	}

	@Override
	public Optional<AppInstance> find(Long appInstanceId) {
		checkParam(appInstanceId);
		return appInstanceRepo.findById(appInstanceId);
	}

	@Override
	public List<AppInstance> findAll() {
		return appInstanceRepo.findAll()
				.stream()
				.filter(appInstance -> !appInstance.getDomain().isDeleted())
				.collect(Collectors.toList());
	}

	@Override
	public Page<AppInstance> findAll(Pageable pageable) {
		Page<AppInstance> page = appInstanceRepo.findAll(pageable);
		List<AppInstance> filtered = page.getContent()
				.stream()
				.filter(appInstance -> !appInstance.getDomain().isDeleted())
				.collect(Collectors.toList());
		return new PageImpl<>(filtered, pageable, filtered.size());
	}

	@Override
	public List<AppInstance> findAllByOwner(Long userId) {
		checkParam(userId);
		User user = users.findById(userId).orElseThrow(() -> new ObjectNotFoundException("user not found"));
		return findAllByOwner(user);
	}

	@Override
	public List<AppInstance> findAllByOwner(User owner) {
		checkParam(owner);
		return appInstanceRepo.findAllByOwner(owner);
	}

	@Override
	public List<AppInstance> findAllByOwner(Long userId, Long domainId) {
		User owner = getUser(userId);
		Domain domain = getDomain(domainId);
		return findAllByOwnerAndDomain(owner, domain);
	}

	@Override
	public List<AppInstance> findAllByOwnerAndDomain(User owner, Domain domain) {
		checkParam(owner);
		checkParam(domain);
		return appInstanceRepo.findAllByOwnerAndDomain(owner, domain);
	}

	@Override
	public Page<AppInstance> findAllByOwner(Long userId, Pageable pageable) {
		User user = getUser(userId);
		return findAllByOwner(user, pageable);
	}

	@Override
	public Page<AppInstance> findAllByOwner(User owner, Pageable pageable) {
		checkParam(owner);
		return appInstanceRepo.findAllByOwner(owner, pageable);
	}

	@Override
	public Page<AppInstance> findAllByOwner(Long userId, Long domainId, Pageable pageable) {
		User owner = getUser(userId);
		Domain domain = getDomain(domainId);
		return findAllByOwner(owner, domain, pageable);
	}

	@Override
	public Page<AppInstance> findAllByOwner(User owner, Domain domain, Pageable pageable) {
		checkParam(owner);
		checkParam(domain);
		return appInstanceRepo.findAllByOwnerAndDomain(owner, domain, pageable);
	}

	@Override
	public List<AppInstance> findAllByDomain(Long domainId) {
		Domain domain = getDomain(domainId);
		return findAllByDomain(domain);
	}

	@Override
	public List<AppInstance> findAllByDomain(Domain domain) {
		checkParam(domain);
		return appInstanceRepo.findAllByDomain(domain);
	}

	@Override
	public Page<AppInstance> findAllByDomain(Long domainId, Pageable pageable) {
		Domain domain = getDomain(domainId);
		return findAllByDomain(domain, pageable);
	}

	@Override
	public Page<AppInstance> findAllByDomain(Domain domain, Pageable pageable) {
		checkParam(domain);
		return appInstanceRepo.findAllByDomain(domain, pageable);
	}

	@Override
	@Transactional
	@Loggable(LogLevel.DEBUG)
	public boolean checkUpgradePossible(Long appInstanceId) {
		Optional<AppInstance> appInstance = appInstanceRepo.findById(appInstanceId);
		if (appInstance.isPresent()) {
			String currentHelmChartVersion = appInstance.get().getApplication().getAppDeploymentSpec().getKubernetesTemplate().getChart().getVersion();
			Map<String, Long> allAppVersions = applications.findAllActiveVersionNumbers(appInstance.get().getApplication().getName());
			return instanceUpgradeService.getNextApplicationVersionForUpgrade(currentHelmChartVersion, allAppVersions).isPresent();
		}
		return false;
	}

	@Override
	@Transactional
	@Loggable(LogLevel.DEBUG)
	public AppInstanceView.AppInstanceUpgradeInfo obtainUpgradeInfo(Long appInstanceId) {
		Optional<AppInstance> appInstance = appInstanceRepo.findById(appInstanceId);
		if (appInstance.isPresent()) {
			String currentHelmChartVersion = appInstance.get().getApplication().getAppDeploymentSpec().getKubernetesTemplate().getChart().getVersion();
			Map<String, Long> allAppVersions = applications.findAllActiveVersionNumbers(appInstance.get().getApplication().getName());
			Optional<Long> nextVersionId = instanceUpgradeService.getNextApplicationVersionForUpgrade(currentHelmChartVersion, allAppVersions);
			if (nextVersionId.isPresent()) {
				Optional<Application> nextApplication = applications.findApplication(nextVersionId.get());
				if (nextApplication.isPresent()) {
					return new AppInstanceView.AppInstanceUpgradeInfo(
							nextVersionId.get(),
							nextApplication.get().getVersion(),
							nextApplication.get().getAppDeploymentSpec().getKubernetesTemplate().getChart().getVersion());
				}
			}
		}
		return null;
	}

	private void checkParam(AppInstance appInstance) {
		if(appInstance == null) {
			throw new IllegalArgumentException("appInstance is null");
		}
		checkParam(appInstance.getId());
	}
	
	private void checkParam(Long id) {
		checkArgument(id != null, "Id is null");
	}

	private void checkParam(Identifier id) {
		checkArgument(id != null, "Id is null");
	}
	
	private void checkParam(Application application) {
		if(application == null) {
			throw new IllegalArgumentException("application is null");
		}
		checkParam(application.getId());
	}
	
	private void checkParam(Domain domain) {
		if(domain == null) {
			throw new IllegalArgumentException("domain is null");
		}
		checkParam(domain.getId());		
	}
	
	private void checkParam(User user) {
		if(user == null) {
			throw new IllegalArgumentException("user is null");
		}
		checkParam(user.getId());
	}

	private void checkNameCharacters(String name){
	    checkArgument(validator.valid(name), "Instance name is not valid");
	}

	protected Domain getDomain(Long domainId) {
		checkParam(domainId);
		return domains.findDomain(domainId).orElseThrow(() -> new ObjectNotFoundException("Domain not found"));
	}
	
	protected User getUser(Long userId) {
		checkParam(userId);
		return users.findById(userId).orElseThrow(() -> new ObjectNotFoundException("User not found"));
	}
	
}
