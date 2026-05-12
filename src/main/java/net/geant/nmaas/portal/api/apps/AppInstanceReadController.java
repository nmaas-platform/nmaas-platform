package net.geant.nmaas.portal.api.apps;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.api.dto.applications.AppInstanceBase;
import net.geant.nmaas.api.dto.applications.AppInstanceState;
import net.geant.nmaas.api.dto.applications.AppInstanceStatus;
import net.geant.nmaas.api.dto.applications.AppInstanceView;
import net.geant.nmaas.api.dto.applications.AppInstanceViewExtended;
import net.geant.nmaas.api.dto.applications.AppInstanceViewExtendedDto;
import net.geant.nmaas.api.dto.applications.ApplicationBaseView;
import net.geant.nmaas.api.dto.applications.ConfigWizardTemplateDto;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.api.model.AppDeploymentHistoryView;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.exceptions.InvalidDomainException;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistence.entity.AppInstance;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UserRole;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationInstanceBaseService;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/apps/instances")
@Slf4j
public class AppInstanceReadController extends AppBaseController {

    private static final String MISSING_APP_INSTANCE_MESSAGE = "Missing app instance";
    private static final String MISSING_USER_MESSAGE = "User not found";
    private static final String DOMAIN_NOT_FOUND_MESSAGE = "Domain %s not found";

    private final AppDeploymentMonitor appDeploymentMonitor;
    private final ApplicationInstanceService applicationInstanceService;
    private final DomainService domainService;
    private final AppDeploymentRepositoryManager appDeploymentRepositoryManager;
    private final ApplicationInstanceBaseService instanceBaseService;

    @Autowired
    public AppInstanceReadController(ModelMapper modelMapper,
                                     ApplicationService applicationService,
                                     ApplicationBaseService appBaseService,
                                     UserService userService,
                                     AppDeploymentMonitor appDeploymentMonitor,
                                     ApplicationInstanceService applicationInstanceService,
                                     DomainService domainService,
                                     AppDeploymentRepositoryManager appDeploymentRepositoryManager,
                                     ApplicationInstanceBaseService instanceBaseService) {
        super(modelMapper, userService, applicationService, appBaseService);
        this.appDeploymentMonitor = appDeploymentMonitor;
        this.applicationInstanceService = applicationInstanceService;
        this.domainService = domainService;
        this.appDeploymentRepositoryManager = appDeploymentRepositoryManager;
        this.instanceBaseService = instanceBaseService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @Transactional
    public List<AppInstanceBase> getAllInstances(@RequestParam(required = false) String status) {
        List<AppInstanceBase> result = applicationInstanceService.findAll().stream()
                .map(this::mapAppInstanceBase)
                .toList();
        List<AppInstanceState> undeployedStates = List.of(AppInstanceState.REMOVED, AppInstanceState.DONE);
        if (status == null || status.equals("deployed")) {
            return result.stream()
                    .filter(instance -> !undeployedStates.contains(instance.getState()))
                    .toList();
        } else if (status.equals("undeployed")) {
            return result.stream()
                    .filter(instance -> undeployedStates.contains(instance.getState()))
                    .toList();
        } else {
            log.warn("Unknown status: {}", status);
            return result;
        }
    }

    @GetMapping(params = {"page"})
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @Transactional
    public Page<AppInstanceBase> getAllInstances(Pageable pageable) {
        logPageable(pageable);
        pageable = pageableValidator(pageable);
        return instanceBaseService.findAll(pageable);
    }

    @GetMapping("/my")
    @Transactional
    public List<AppInstanceBase> getMyAllInstances(@NotNull Principal principal) {
        User user = userService.findByUsername(principal.getName()).orElseThrow(() ->
                new MissingElementException(MISSING_USER_MESSAGE));
        return applicationInstanceService.findAllByOwner(user).stream()
                .map(this::mapAppInstanceBase)
                .toList();
    }

    @GetMapping(value = "/my", params = {"page"})
    @Transactional
    public Page<AppInstanceBase> getMyAllInstances(@NotNull Principal principal,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(required = false, defaultValue = "") String search,
                                                   Pageable pageable) {
        logPageable(pageable);
        pageable = pageableValidator(pageable);
        User user = userService.findByUsername(principal.getName()).orElseThrow(() ->
                new MissingElementException(MISSING_USER_MESSAGE));
        if (status != null) {
            return instanceBaseService.findAllByOwner(user, pageable, status.equals("deployed"), search);
        }
        return instanceBaseService.findAllByOwner(user, pageable, search);
    }

    @GetMapping("/domain/{domainId}")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'ANY')")
    @Transactional
    public List<AppInstanceBase> getAllInstances(@PathVariable Long domainId,
                                                 @NotNull Principal principal,
                                                 @RequestParam(required = false) String status) {
        List<AppInstanceBase> result;
        Domain domain = domainService.findDomain(domainId)
                .orElseThrow(() -> new MissingElementException(String.format(DOMAIN_NOT_FOUND_MESSAGE, domainId)));
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException(MISSING_USER_MESSAGE));

        if (this.isSystemAdminAndIsDomainGlobal(user, domainId)) {
            result = applicationInstanceService.findAll().stream()
                    .map(this::mapAppInstanceBase)
                    .toList();
        } else {
            result = applicationInstanceService.findAllByDomain(domain).stream()
                    .map(this::mapAppInstanceBase)
                    .toList();
        }

        if (status != null && status.equals("deployed")) {
            return result.stream()
                    .filter(instance -> instance.getState() != AppInstanceState.REMOVED && instance.getState() != AppInstanceState.DONE)
                    .toList();
        } else if (status != null && status.equals("undeployed")) {
            return result.stream()
                    .filter(instance -> List.of(AppInstanceState.REMOVED, AppInstanceState.DONE).contains(instance.getState()))
                    .toList();
        }

        return result;
    }

    @GetMapping(value = "/domain/{domainId}", params = {"page"})
    @PreAuthorize("hasPermission(#domainId, 'domain', 'ANY')")
    @Transactional
    public Page<AppInstanceBase> getAllInstances(@PathVariable Long domainId,
                                                 @NotNull Principal principal,
                                                 Pageable pageable,
                                                 @RequestParam(required = false) String status,
                                                 @RequestParam(required = false, defaultValue = "") String search) {
        logPageable(pageable);
        Domain domain = domainService.findDomain(domainId)
                .orElseThrow(() -> new MissingElementException(String.format(DOMAIN_NOT_FOUND_MESSAGE, domainId)));
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException(MISSING_USER_MESSAGE));

        if (this.isSystemAdminAndIsDomainGlobal(user, domainId)) {
            if (status != null) {
                return instanceBaseService.findAll(pageable, status.equals("deployed"), search);
            }
            return instanceBaseService.findAll(pageable);
        } else {
            if (status != null) {
                return instanceBaseService.findAllByDomain(domain, pageable, status.equals("deployed"), search);
            }
            return instanceBaseService.findAllByDomain(domain, pageable, search);
        }
    }

    @GetMapping("/running/domain/{domainId}")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'ANY')")
    @Transactional
    public List<AppInstanceView> getRunningAppInstances(@PathVariable(value = "domainId") long domainId,
                                                        @NotNull Principal principal) {
        Domain domain = domainService.findDomain(domainId).orElseThrow(() -> new InvalidDomainException("Domain not found"));
        return getAllRunningByDomain(domain);
    }

    @GetMapping(value = "/running/domain/{domainId}", params = {"page"})
    @PreAuthorize("hasPermission(#domainId, 'domain', 'ANY')")
    @Transactional
    public Page<AppInstanceView> getRunningAppInstances(@PathVariable(value = "domainId") long domainId,
                                                        @NotNull Principal principal,
                                                        Pageable pageable) {
        logPageable(pageable);
        pageable = pageableValidator(pageable);
        Domain domain = domainService.findDomain(domainId).orElseThrow(() -> new InvalidDomainException("Domain not found"));
        return getAllRunningByDomain(domain, pageable);
    }

    @GetMapping("/running/app/{id}")
    @Transactional
    public boolean hasRunningInstance(@PathVariable Long id) {
        ApplicationBase appBase = applicationBaseService.getBaseApp(id);
        return appBase.getVersions().stream()
                .map(version -> applicationService.findApplication(version.getAppVersionId())
                        .orElseThrow(() -> new RuntimeException("Application not found")))
                .map(applicationInstanceService::findAllByApplication)
                .flatMap(List::stream)
                .anyMatch(this::isInstanceRunning);
    }

    @GetMapping(value = "/domain/{domainId}/my")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'ANY')")
    @Transactional
    public List<AppInstanceBase> getMyAllInstances(@PathVariable Long domainId,
                                                   @NotNull Principal principal,
                                                   @RequestParam(required = false) String status) {
        User user = userService.findByUsername(principal.getName()).orElseThrow(() -> new UsernameNotFoundException(MISSING_USER_MESSAGE));

        if (this.isSystemAdminAndIsDomainGlobal(user, domainId)) {
            if (status != null && status.equals("deployed")) {
                return applicationInstanceService.findAllByOwner(user).stream()
                        .map(this::mapAppInstanceBase)
                        .filter(appInstanceBase ->
                                appInstanceBase.getState() != AppInstanceState.REMOVED &&
                                        appInstanceBase.getState() != AppInstanceState.DONE)
                        .toList();
            } else if (status != null && status.equals("undeployed")) {
                return applicationInstanceService.findAllByOwner(user).stream()
                        .map(this::mapAppInstanceBase)
                        .filter(appInstanceBase ->
                                appInstanceBase.getState() == AppInstanceState.REMOVED ||
                                        appInstanceBase.getState() == AppInstanceState.DONE)
                        .toList();
            }
            return applicationInstanceService.findAllByOwner(user).stream()
                    .map(this::mapAppInstanceBase)
                    .toList();
        } else {
            if (status != null && status.equals("deployed")) {
                return getUserDomainAppInstances(domainId, principal.getName())
                        .stream().filter(appInstanceBase ->
                                appInstanceBase.getState() != AppInstanceState.REMOVED &&
                                        appInstanceBase.getState() != AppInstanceState.DONE)
                        .toList();
            } else if (status != null && status.equals("undeployed")) {
                return getUserDomainAppInstances(domainId, principal.getName())
                        .stream().filter(appInstanceBase ->
                                appInstanceBase.getState() == AppInstanceState.REMOVED ||
                                        appInstanceBase.getState() == AppInstanceState.DONE)
                        .toList();
            }
            return getUserDomainAppInstances(domainId, principal.getName());
        }
    }

    @GetMapping(value = "/domain/{domainId}/my", params = {"page"})
    @PreAuthorize("hasPermission(#domainId, 'domain', 'ANY')")
    @Transactional
    public Page<AppInstanceBase> getMyAllInstances(@PathVariable Long domainId,
                                                   @NotNull Principal principal,
                                                   Pageable pageable,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(required = false, defaultValue = "") String search) {
        logPageable(pageable);
        pageable = pageableValidator(pageable);
        User user = userService.findByUsername(principal.getName()).orElseThrow(() -> new UsernameNotFoundException(MISSING_USER_MESSAGE));

        if (this.isSystemAdminAndIsDomainGlobal(user, domainId)) {
            if (status != null) {
                return instanceBaseService.findAllByOwner(user, pageable, status.equals("deployed"), search);
            }
            return instanceBaseService.findAllByOwner(user, pageable);
        } else {
            if (status != null) {
                return getPageUserDomainAppInstances(domainId, principal.getName(), pageable, status.equals("deployed"), search);
            }
            return getPageUserDomainAppInstances(domainId, principal.getName(), pageable);
        }
    }

    @GetMapping("/domain/{domainId}/user/{username}")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
    @Transactional
    public List<AppInstanceBase> getUserAllInstances(@PathVariable Long domainId, @PathVariable String username) {
        return getUserDomainAppInstances(domainId, username);
    }

    @GetMapping(value = "/domain/{domainId}/user/{username}", params = {"page"})
    @PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
    @Transactional
    public Page<AppInstanceBase> getUserAllInstances(@PathVariable Long domainId,
                                                     @PathVariable String username,
                                                     Pageable pageable) {
        logPageable(pageable);
        pageable = pageableValidator(pageable);
        return getPageUserDomainAppInstances(domainId, username, pageable);
    }

    @GetMapping("/{appInstanceId}")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'READ')")
    @Transactional
    public AppInstanceViewExtendedDto getAppInstance(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                                     @NotNull Principal principal) {
        AppInstance appInstance = applicationInstanceService.find(appInstanceId)
                .orElseThrow(() -> new MissingElementException("App instance not found."));
        return new AppInstanceViewExtendedDto(mapAppInstanceExtended(appInstance));
    }

    @GetMapping("/{appInstanceId}/state")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'READ')")
    @Transactional
    public AppInstanceStatus getState(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                      @NotNull Principal principal) {
        AppInstance appInstance = findAppInstance(appInstanceId);
        return getAppInstanceState(appInstance);
    }

    @GetMapping("/{appInstanceId}/state/history")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'READ')")
    @Transactional
    public List<AppDeploymentHistoryView> getStateHistory(@PathVariable(value = "appInstanceId") Long appInstanceId) {
        try {
            AppInstance appInstance = findAppInstance(appInstanceId);
            return appDeploymentMonitor.appDeploymentHistory(appInstance.getInternalId());
        } catch (InvalidDeploymentIdException e) {
            throw new MissingElementException(e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public Map<String, Long> deploymentStatistics() {
        return appDeploymentRepositoryManager.getDeploymentStatistics();
    }

    @GetMapping("/{appInstanceId}/parameters")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    public Map<String, String> getDeploymentParameters(@PathVariable Long appInstanceId) {
        Identifier internalId = findAppInstance(appInstanceId).getInternalId();
        return appDeploymentMonitor.appDeploymentParameters(internalId);
    }

    private List<AppInstanceView> getAllRunningByDomain(Domain domain) {
        return applicationInstanceService.findAllByDomain(domain).stream()
                .filter(this::isInstanceRunning)
                .map(this::mapAppInstance)
                .toList();
    }

    private Page<AppInstanceView> getAllRunningByDomain(Domain domain, Pageable pageable) {
        Page<AppInstance> page = applicationInstanceService.findAllByDomain(domain, pageable);
        List<AppInstanceView> filtered = page.getContent()
                .stream()
                .filter(this::isInstanceRunning)
                .map(this::mapAppInstance)
                .toList();
        return new PageImpl<>(filtered, pageable, filtered.size());
    }

    private boolean isInstanceRunning(AppInstance app) {
        return appDeploymentMonitor.state(app.getInternalId()).equals(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED);
    }

    private List<AppInstanceBase> getUserDomainAppInstances(Long domainId, String username) {
        Domain domain = domainService.findDomain(domainId)
                .orElseThrow(() -> new MissingElementException(String.format(DOMAIN_NOT_FOUND_MESSAGE, domainId)));
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new MissingElementException(MISSING_USER_MESSAGE));
        return applicationInstanceService.findAllByOwner(user.getId(), domain.getId()).stream()
                .map(this::mapAppInstanceBase)
                .toList();
    }

    private Page<AppInstanceBase> getPageUserDomainAppInstances(Long domainId,
                                                                String username,
                                                                Pageable pageable) {
        Domain domain = domainService.findDomain(domainId)
                .orElseThrow(() -> new MissingElementException(String.format(DOMAIN_NOT_FOUND_MESSAGE, domainId)));
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new MissingElementException(MISSING_USER_MESSAGE));
        return instanceBaseService.findAllByOwner(user, domain, pageable);
    }

    private Page<AppInstanceBase> getPageUserDomainAppInstances(Long domainId,
                                                                String username,
                                                                Pageable pageable,
                                                                boolean deployed,
                                                                String search) {
        Domain domain = domainService.findDomain(domainId)
                .orElseThrow(() -> new MissingElementException(String.format(DOMAIN_NOT_FOUND_MESSAGE, domainId)));
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new MissingElementException(MISSING_USER_MESSAGE));
        return instanceBaseService.findAllByOwner(user, domain, pageable, deployed, search);
    }

    private AppInstanceStatus getAppInstanceState(AppInstance appInstance) {
        if (appInstance == null) {
            throw new MissingElementException("App instance is null");
        }
        try {
            return prepareAppInstanceStatus(
                    appInstance.getId(),
                    appDeploymentMonitor.state(appInstance.getInternalId()),
                    appDeploymentMonitor.previousState(appInstance.getInternalId()));
        } catch (InvalidDeploymentIdException e) {
            throw new ProcessingException(MISSING_APP_INSTANCE_MESSAGE);
        }
    }

    private AppInstanceStatus prepareAppInstanceStatus(Long appInstanceId, AppLifecycleState state, AppLifecycleState previousState) {
        AppInstanceState appInstanceState = AppInstanceController.mapAppInstanceState(state);
        return new AppInstanceStatus(appInstanceId, appInstanceState, AppInstanceController.mapAppInstanceState(previousState),
                state.name(), state.getUserFriendlyState(), appInstanceState.getUserFriendlyState());
    }

    private AppInstance findAppInstance(Long appInstanceId) {
        if (appInstanceId == null) {
            throw new MissingElementException("Missing app instance identifier");
        }
        return applicationInstanceService.find(appInstanceId).orElseThrow(() -> new MissingElementException("App instance not found."));
    }

    private AppInstanceView mapAppInstance(AppInstance appInstance) {
        if (appInstance == null) {
            return null;
        }
        AppInstanceView ai = modelMapper.map(appInstance, AppInstanceView.class);
        return this.addAppInstanceProperties(ai, appInstance);
    }

    private AppInstanceViewExtended mapAppInstanceExtended(AppInstance appInstance) {
        if (appInstance == null) {
            return null;
        }
        AppInstanceViewExtended ai = modelMapper.map(appInstance, AppInstanceViewExtended.class);
        ApplicationBase applicationBase = applicationBaseService.findByVersionId(appInstance.getApplication().getId());
        ai.getApplication().setApplicationBase(modelMapper.map(applicationBase, ApplicationBaseView.class));
        return (AppInstanceViewExtended) addAppInstanceProperties(ai, appInstance);
    }

    private AppInstanceBase mapAppInstanceBase(AppInstance appInstance) {
        if (appInstance == null) {
            return null;
        }
        AppInstanceBase ai = modelMapper.map(appInstance, AppInstanceBase.class);
        ai.setApplicationBaseId(applicationBaseService.findByVersionId(appInstance.getApplication().getId()).getId());
        return addAppInstanceBaseProperties(ai, appInstance);
    }

    private AppInstanceBase addAppInstanceBaseProperties(AppInstanceBase ai, AppInstance appInstance) {
        try {
            ai.setState(AppInstanceController.mapAppInstanceState(this.appDeploymentMonitor.state(appInstance.getInternalId())));
            ai.setUserFriendlyState(ai.getState().getUserFriendlyState());
        } catch (Exception e) {
            ai.setState(AppInstanceState.UNKNOWN);
            ai.setUserFriendlyState(ai.getState().getUserFriendlyState());
        }

        if (!ai.getDomainId().equals(appInstance.getDomain().getId())) {
            ai.setDomainId(appInstance.getDomain().getId());
        }

        if (!List.of(AppInstanceState.DONE, AppInstanceState.REMOVED).contains(ai.getState())) {
            ai.setUpgradePossible(applicationInstanceService.checkUpgradePossible(appInstance.getId()));
        }

        return ai;
    }

    private AppInstanceView addAppInstanceProperties(AppInstanceView ai, AppInstance appInstance) {
        addAppInstanceBaseProperties(ai, appInstance);

        Identifier identifier = appInstance.getInternalId();
        try {
            ai.setServiceAccessMethods(appDeploymentMonitor.userAccessDetails(identifier).getServiceAccessMethods());
        } catch (InvalidAppStateException | InvalidDeploymentIdException e) {
            ai.setServiceAccessMethods(null);
        }

        try {
            ai.setAppConfigRepositoryAccessDetails(appDeploymentMonitor.configRepositoryAccessDetails(identifier));
        } catch (InvalidAppStateException | InvalidDeploymentIdException e) {
            ai.setAppConfigRepositoryAccessDetails(null);
        }

        try {
            ai.setDescriptiveDeploymentId(appDeploymentRepositoryManager.load(appInstance.getInternalId()).getDescriptiveDeploymentId().value());
        } catch (InvalidDeploymentIdException e) {
            ai.setDescriptiveDeploymentId(null);
        }

        try {
            ai.setConfigWizardTemplate(new ConfigWizardTemplateDto(
                    appInstance.getApplication().getConfigWizardTemplate().getId(),
                    appInstance.getApplication().getConfigWizardTemplate().getTemplate()));
        } catch (Exception e) {
            ai.setConfigWizardTemplate(null);
        }

        try {
            ai.setConfigUpdateWizardTemplate(new ConfigWizardTemplateDto(
                    appInstance.getApplication().getConfigUpdateWizardTemplate().getId(),
                    appInstance.getApplication().getConfigUpdateWizardTemplate().getTemplate()));
        } catch (Exception e) {
            ai.setConfigUpdateWizardTemplate(null);
        }

        ai.setUpgradeInfo(applicationInstanceService.obtainUpgradeInfo(appInstance.getId()));

        return ai;
    }

    private static void logPageable(Pageable p) {
        log.trace("Page number: {}\tPage size:{}\tPage offset:{}\tSort:{}", p.getPageNumber(), p.getPageSize(), p.getOffset(), p.getSort());
    }

    private boolean isSystemAdminAndIsDomainGlobal(User user, Long domainId) {
        boolean isSystemAdmin = false;
        boolean isDomainGlobal = false;
        if (user.getRoles().stream().anyMatch((UserRole ur) -> ur.getRole().equals(Role.ROLE_SYSTEM_ADMIN))) {
            isSystemAdmin = true;
        }
        if (domainId.equals(domainService.getGlobalDomain().orElseThrow(() -> new InvalidDomainException("Global Domain not found")).getId())) {
            isDomainGlobal = true;
        }
        return isSystemAdmin && isDomainGlobal;
    }

    private static Pageable pageableValidator(Pageable pageable) {
        if (!isPageableValidForAppInstance(pageable)) {
            return null;
        }
        return pageable;
    }

    private static boolean isPageableValidForAppInstance(Pageable p) {
        List<String> sortProperties = p.getSort().get()
                .map(Sort.Order::getProperty)
                .toList();
        List<String> classProperties = Arrays.stream(AppInstance.class.getDeclaredFields())
                .map(Field::getName)
                .toList();

        Set<String> sortSet = new HashSet<>(sortProperties);
        Set<String> classSet = new HashSet<>(classProperties);

        sortSet.removeAll(classSet);

        return sortSet.isEmpty();
    }

}
