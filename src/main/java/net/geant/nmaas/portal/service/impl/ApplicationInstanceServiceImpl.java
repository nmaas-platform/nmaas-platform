package net.geant.nmaas.portal.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.api.dto.applications.AppConfigurationDto;
import net.geant.nmaas.api.dto.applications.AppInstanceDto;
import net.geant.nmaas.api.dto.applications.AppInstanceState;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidApplicationIdException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.api.apps.AppInstanceController;
import net.geant.nmaas.portal.exceptions.ApplicationSubscriptionNotActiveException;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.persistence.entity.AppInstance;
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.entity.ApplicationStatePerDomain;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.ApplicationInstanceUpgradeService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.ApplicationStatePerDomainService;
import net.geant.nmaas.portal.service.ApplicationSubscriptionService;
import net.geant.nmaas.portal.service.CodenameValidator;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ApplicationInstanceServiceImpl implements ApplicationInstanceService {

    private final AppInstanceRepository appInstanceRepository;
    private final ApplicationService applicationService;
    private final DomainService domainService;
    private final UserService userService;
    private final ApplicationSubscriptionService applicationSubscriptions;
    private final CodenameValidator validator;
    private final ApplicationStatePerDomainService applicationStatePerDomainService;
    private final ApplicationInstanceUpgradeService instanceUpgradeService;
    private final AppLifecycleManager appLifecycleManager;
    private final ConfigurationManager configurationManager;
    private final AppDeploymentMonitor appDeploymentMonitor;

    @Autowired
    public ApplicationInstanceServiceImpl(
            AppInstanceRepository appInstanceRepository,
            ApplicationService applicationService,
            DomainService domainService,
            UserService userService,
            ApplicationSubscriptionService applicationSubscriptions,
            @Qualifier("instanceNameValidator") CodenameValidator validator,
            ApplicationStatePerDomainService applicationStatePerDomainService,
            ApplicationInstanceUpgradeService instanceUpgradeService,
            AppLifecycleManager appLifecycleManager,
            ConfigurationManager configurationManager,
            AppDeploymentMonitor appDeploymentMonitor
    ) {
        this.appInstanceRepository = appInstanceRepository;
        this.applicationService = applicationService;
        this.domainService = domainService;
        this.userService = userService;
        this.applicationSubscriptions = applicationSubscriptions;
        this.validator = validator;
        this.applicationStatePerDomainService = applicationStatePerDomainService;
        this.instanceUpgradeService = instanceUpgradeService;
        this.appLifecycleManager = appLifecycleManager;
        this.configurationManager = configurationManager;
        this.appDeploymentMonitor = appDeploymentMonitor;
    }

    @Override
    public AppInstance create(Long domainId, Long applicationId, String name, boolean autoUpgradesEnabled) {
        Application app = applicationService.findApplication(applicationId).orElseThrow(() -> new ObjectNotFoundException("Application not found."));
        Domain domain = domainService.findDomain(domainId).orElseThrow(() -> new ObjectNotFoundException("Domain not found."));
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

        if (!applicationStatePerDomainService.isApplicationEnabledInDomain(domain, application)) {
            throw new IllegalArgumentException("Application is disabled in domain settings");
        }

        if (applicationSubscriptions.isActive(application.getName(), domain)) {
            return appInstanceRepository.save(new AppInstance(application, domain, name, autoUpgradesEnabled));
        } else {
            throw new ApplicationSubscriptionNotActiveException("Application subscription is missing or not active.");
        }
    }

    @Override
    public boolean validateAgainstAppConfiguration(AppInstance appInstance, AppConfigurationDto appConfigurationView) {
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
        find(appInstanceId).ifPresent(appInstanceRepository::delete);
    }

    @Override
    public void update(AppInstance appInstance) {
        checkParam(appInstance);
        appInstanceRepository.save(appInstance);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateApplication(Identifier internalId, Long applicationId) {
        checkParam(internalId);
        checkParam(applicationId);
        final AppInstance instance = findByInternalId(internalId)
                .orElseThrow(() -> new InvalidDeploymentIdException("Application instance with internalId " + internalId + " does not exist"));
        final Application application = applicationService.findApplication(applicationId)
                .orElseThrow(() -> new InvalidApplicationIdException("Application with id " + applicationId + " does not exist"));
        appInstanceRepository.updateApplication(instance.getId(), instance.getApplication().getId(), application);
    }

    @Override
    public Optional<AppInstance> findByInternalId(Identifier deploymentId) {
        checkParam(deploymentId);
        return appInstanceRepository.findByInternalId(deploymentId);
    }

    @Override
    public Optional<AppInstance> find(Long appInstanceId) {
        checkParam(appInstanceId);
        return appInstanceRepository.findById(appInstanceId);
    }

    @Override
    public List<AppInstance> findAll() {
        return appInstanceRepository.findAll()
                .stream()
                .filter(appInstance -> !appInstance.getDomain().isDeleted())
                .collect(Collectors.toList());
    }

    @Override
    public Page<AppInstance> findAll(Pageable pageable) {
        Page<AppInstance> page = appInstanceRepository.findAll(pageable);
        List<AppInstance> filtered = page.getContent()
                .stream()
                .filter(appInstance -> !appInstance.getDomain().isDeleted())
                .collect(Collectors.toList());
        return new PageImpl<>(filtered, pageable, filtered.size());
    }

    @Override
    public List<AppInstance> findAllByOwner(Long userId) {
        checkParam(userId);
        User user = userService.findById(userId).orElseThrow(() -> new ObjectNotFoundException("user not found"));
        return findAllByOwner(user);
    }

    @Override
    public List<AppInstance> findAllByOwner(User owner) {
        checkParam(owner);
        return appInstanceRepository.findAllByOwner(owner);
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
        return appInstanceRepository.findAllByOwnerAndDomain(owner, domain);
    }

    @Override
    public Page<AppInstance> findAllByOwner(Long userId, Pageable pageable) {
        User user = getUser(userId);
        return findAllByOwner(user, pageable);
    }

    @Override
    public Page<AppInstance> findAllByOwner(User owner, Pageable pageable) {
        checkParam(owner);
        return appInstanceRepository.findAllByOwner(owner, pageable);
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
        return appInstanceRepository.findAllByOwnerAndDomain(owner, domain, pageable);
    }

    @Override
    public List<AppInstance> findAllByDomain(Long domainId) {
        Domain domain = getDomain(domainId);
        return findAllByDomain(domain);
    }

    @Override
    public List<AppInstance> findAllByDomain(Domain domain) {
        checkParam(domain);
        return appInstanceRepository.findAllByDomain(domain);
    }

    @Override
    public Page<AppInstance> findAllByDomain(Long domainId, Pageable pageable) {
        Domain domain = getDomain(domainId);
        return findAllByDomain(domain, pageable);
    }

    @Override
    public Page<AppInstance> findAllByDomain(Domain domain, Pageable pageable) {
        checkParam(domain);
        return appInstanceRepository.findAllByDomain(domain, pageable);
    }

    @Override
    public List<AppInstance> findAllByApplication(Application application) {
        return appInstanceRepository.findAllByApplication(application);
    }

    @Override
    @Transactional
    @Loggable(LogLevel.TRACE)
    public boolean checkUpgradePossible(Long appInstanceId) {
        return obtainVersionForUpgrade(appInstanceId).isPresent();
    }

    @Override
    @Transactional
    @Loggable(LogLevel.TRACE)
    public boolean checkUpgradePossible(Long appInstanceId, String targetVersion) {
        return obtainVersionForUpgrade(appInstanceId)
                .map(application -> application.getVersion().equals(targetVersion))
                .orElse(false);
    }

    private Optional<Application> obtainVersionForUpgrade(Long appInstanceId) {
        Optional<AppInstance> appInstance = appInstanceRepository.findById(appInstanceId);
        if (appInstance.isPresent()) {
            String currentHelmChartVersion = appInstance.get().getApplication().getAppDeploymentSpec().getKubernetesTemplate().getChart().getVersion();
            Map<String, Long> allAppVersions = applicationService.findAllActiveVersionNumbers(appInstance.get().getApplication().getName());
            Optional<Long> versionForUpgrade = instanceUpgradeService.getNextApplicationVersionForUpgrade(currentHelmChartVersion, allAppVersions);
            return versionForUpgrade.flatMap(applicationService::findApplication);
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    @Loggable(LogLevel.DEBUG)
    public AppInstanceDto.AppInstanceUpgradeInfo obtainUpgradeInfo(Long appInstanceId) {
        Optional<AppInstance> appInstance = appInstanceRepository.findById(appInstanceId);
        if (appInstance.isPresent()) {
            String currentHelmChartVersion = appInstance.get().getApplication().getAppDeploymentSpec().getKubernetesTemplate().getChart().getVersion();
            Map<String, Long> allAppVersions = applicationService.findAllActiveVersionNumbers(appInstance.get().getApplication().getName());
            Optional<Long> nextVersionId = instanceUpgradeService.getNextApplicationVersionForUpgrade(currentHelmChartVersion, allAppVersions);
            if (nextVersionId.isPresent()) {
                Optional<Application> nextApplication = applicationService.findApplication(nextVersionId.get());
                if (nextApplication.isPresent()) {
                    return new AppInstanceDto.AppInstanceUpgradeInfo(
                            nextVersionId.get(),
                            nextApplication.get().getVersion(),
                            nextApplication.get().getAppDeploymentSpec().getKubernetesTemplate().getChart().getVersion());
                }
            }
        }
        return null;
    }

    @Override
    @Loggable(LogLevel.DEBUG)
    public void deleteAllByDomain(Long domainId) {
        List<AppInstance> appsByDomain = findAllByDomain(domainId);
        Iterator<AppInstance> iterator = appsByDomain.iterator();
        while (iterator.hasNext()) {
            AppInstance app = iterator.next();
            delete(app.getId());
            appLifecycleManager.removeApplication(app.getInternalId());
            iterator.remove();
        }
    }

    @Override
    public boolean isNameAvailableInDomain(String name, Domain domain) {
        log.error("Found: {} matching names in domain", appInstanceRepository.isNameAvailableInDomain(name, domain.getCodename()));
        return appInstanceRepository.isNameAvailableInDomain(name, domain.getCodename()) <= 0;
    }

    @Override
    public boolean isInAnyState(Long appInstanceId, List<AppInstanceState> states) {
        final AppInstance appInstance = find(appInstanceId).orElseThrow(() -> new ObjectNotFoundException("App instance not found"));
        final AppInstanceState currentState = AppInstanceController.mapAppInstanceState(appDeploymentMonitor.state(appInstance.getInternalId()));
        return states.contains(currentState);
    }

    private void checkParam(AppInstance appInstance) {
        if (appInstance == null) {
            throw new IllegalArgumentException("appInstance is null");
        }
        checkParam(appInstance.getId());
    }

    private void checkParam(Long id) {
        Validate.isTrue(id != null, "Id is null");
    }

    private void checkParam(Identifier id) {
        Validate.isTrue(id != null, "Id is null");
    }

    private void checkParam(Application application) {
        if (application == null) {
            throw new IllegalArgumentException("application is null");
        }
        checkParam(application.getId());
    }

    private void checkParam(Domain domain) {
        if (domain == null) {
            throw new IllegalArgumentException("domain is null");
        }
        checkParam(domain.getId());
    }

    private void checkParam(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user is null");
        }
        checkParam(user.getId());
    }

    private void checkNameCharacters(String name) {
        Validate.isTrue(validator.valid(name, configurationManager.getConfiguration().getAppInstanceNameLengthLimit()), "Instance name is not valid");
    }

    protected Domain getDomain(Long domainId) {
        checkParam(domainId);
        return domainService.findDomain(domainId).orElseThrow(() -> new ObjectNotFoundException("Domain not found"));
    }

    protected User getUser(Long userId) {
        checkParam(userId);
        return userService.findById(userId).orElseThrow(() -> new ObjectNotFoundException("User not found"));
    }

}
