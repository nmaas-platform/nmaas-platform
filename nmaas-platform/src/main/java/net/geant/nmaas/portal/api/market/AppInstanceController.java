package net.geant.nmaas.portal.api.market;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.api.model.AppDeploymentHistoryView;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.exceptions.InvalidDomainException;
import net.geant.nmaas.portal.api.domain.AppDeploymentSpecView;
import net.geant.nmaas.portal.api.domain.AppInstanceRequest;
import net.geant.nmaas.portal.api.domain.AppInstanceState;
import net.geant.nmaas.portal.api.domain.AppInstanceStatus;
import net.geant.nmaas.portal.api.domain.AppInstanceView;
import net.geant.nmaas.portal.api.domain.AppInstanceViewExtended;
import net.geant.nmaas.portal.api.domain.ConfigWizardTemplateView;
import net.geant.nmaas.portal.api.domain.Id;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.exceptions.ApplicationSubscriptionNotActiveException;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.DomainService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/apps/instances")
@AllArgsConstructor
@Log4j2
public class AppInstanceController extends AppBaseController {

    private static final String MISSING_APP_INSTANCE_MESSAGE = "Missing app instance";

    private static final String MISSING_USER_MESSAGE = "User not found";

    private AppLifecycleManager appLifecycleManager;

    private AppDeploymentMonitor appDeploymentMonitor;

    private ApplicationInstanceService instances;

    private DomainService domains;

    private AppDeploymentRepositoryManager appDeploymentRepositoryManager;

    /*
    NOTICE:
    NMAAS-756
    temporary fix on pagination size issue involves changing default pagination size in application.properties
    to mitigate this issue in future, it is advised to implement server-side pagination,
    currently both api and user interface does not support this feature
     */

    @GetMapping
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @Transactional
    public List<AppInstanceView> getAllInstances(Pageable pageable) {
        this.logPageable(pageable);
        pageable = this.pageableValidator(pageable);
        return instances.findAll(pageable).getContent().stream()
                .map(this::mapAppInstance)
                .collect(Collectors.toList());
    }

    @GetMapping("/my")
    @Transactional
    public List<AppInstanceView> getMyAllInstances(@NotNull Principal principal, Pageable pageable) {
        this.logPageable(pageable);
        pageable = this.pageableValidator(pageable);
        User user = users.findByUsername(principal.getName()).orElseThrow(() -> new MissingElementException(MISSING_USER_MESSAGE));
        return instances.findAllByOwner(user, pageable).getContent().stream()
                .map(this::mapAppInstance)
                .collect(Collectors.toList());
    }

    @GetMapping("/domain/{domainId}")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'ANY')")
    @Transactional
    public List<AppInstanceView> getAllInstances(@PathVariable Long domainId, @NotNull Principal principal, Pageable pageable) {
        this.logPageable(pageable);
        pageable = this.pageableValidator(pageable);
        Domain domain = domains.findDomain(domainId).orElseThrow(() -> new MissingElementException("Domain " + domainId + " not found"));
        User user = this.users.findByUsername(principal.getName()).orElseThrow(() -> new UsernameNotFoundException(MISSING_USER_MESSAGE));

        // system admin on global view has an overall view over all instances
        if(this.isSystemAdminAndIsDomainGlobal(user, domainId)) {
            return instances.findAll(pageable).getContent().stream()
                    .map(this::mapAppInstance)
                    .collect(Collectors.toList());
        } else {
            return instances.findAllByDomain(domain, pageable).getContent().stream()
                    .map(this::mapAppInstance)
                    .collect(Collectors.toList());
        }
    }

    @GetMapping("/running/domain/{domainId}")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'ANY')")
    @Transactional
    public List<AppInstanceView> getRunningAppInstances(@PathVariable(value = "domainId") long domainId, Principal principal) {
        Domain domain = this.domains.findDomain(domainId).orElseThrow(() -> new InvalidDomainException("Domain not found"));
        User owner = this.users.findByUsername(principal.getName()).orElseThrow(() -> new UsernameNotFoundException(MISSING_USER_MESSAGE));
        return getAllRunningInstancesByOwnerAndDomain(owner, domain);
    }

    private List<AppInstanceView> getAllRunningInstancesByOwnerAndDomain(User owner, Domain domain){
        return this.instances.findAllByOwnerAndDomain(owner, domain).stream()
                .filter(app -> appDeploymentMonitor.state(app.getInternalId()).equals(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED))
                .map(this::mapAppInstance)
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/domain/{domainId}/my")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'ANY')")
    @Transactional
    public List<AppInstanceView> getMyAllInstances(@PathVariable Long domainId, @NotNull Principal principal, Pageable pageable) {
        this.logPageable(pageable);
        pageable = this.pageableValidator(pageable);
        User user = this.users.findByUsername(principal.getName()).orElseThrow(() -> new UsernameNotFoundException(MISSING_USER_MESSAGE));

        if(this.isSystemAdminAndIsDomainGlobal(user, domainId)) {
            return instances.findAllByOwner(user, pageable).getContent().stream()
                    .map(this::mapAppInstance)
                    .collect(Collectors.toList());

        } else {
            return getUserDomainAppInstances(domainId, principal.getName(), pageable);
        }

    }

    @GetMapping("/domain/{domainId}/user/{username}")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
    @Transactional
    public List<AppInstanceView> getUserAllInstances(@PathVariable Long domainId, @PathVariable String username, Pageable pageable){
        this.logPageable(pageable);
        pageable = this.pageableValidator(pageable);
        return getUserDomainAppInstances(domainId, username, pageable);
    }

    private List<AppInstanceView> getUserDomainAppInstances(Long domainId, String username, Pageable pageable) {
        Domain domain = domains.findDomain(domainId)
                .orElseThrow(() -> new MissingElementException("Domain " + domainId + " not found"));
        User user = users.findByUsername(username)
                .orElseThrow(() -> new MissingElementException(MISSING_USER_MESSAGE));
        return instances.findAllByOwner(user, domain, pageable).getContent().stream()
                .map(this::mapAppInstance)
                .collect(Collectors.toList());
    }

    @GetMapping("/{appInstanceId}")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public AppInstanceViewExtended getAppInstance(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                                  @NotNull Principal principal) {
        AppInstance appInstance = instances.find(appInstanceId)
                .orElseThrow(() -> new MissingElementException("App instance not found."));
        return mapAppInstanceExtended(appInstance);
    }

    @PostMapping("/domain/{domainId}")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'CREATE')")
    @Transactional
    public Id createAppInstance(@RequestBody AppInstanceRequest appInstanceRequest,
                                @NotNull Principal principal, @PathVariable Long domainId) {
        Application app = getApp(appInstanceRequest.getApplicationId());
        Domain domain = domains.findDomain(domainId)
                .orElseThrow(() -> new MissingElementException("Domain not found"));
        AppInstance appInstance;
        try {
            appInstance = instances.create(domain, app, appInstanceRequest.getName());
        } catch (ApplicationSubscriptionNotActiveException e) {
            throw new ProcessingException("Unable to create instance. " + e.getMessage());
        }

        AppDeploymentSpecView appDeploymentSpec = modelMapper.map(app.getAppDeploymentSpec(), AppDeploymentSpecView.class);
        AppDeployment appDeployment = AppDeployment.builder()
                .domain(domain.getCodename())
                .instanceId(appInstance.getId())
                .applicationId(Identifier.newInstance(appInstance.getApplication().getId()))
                .deploymentName(appInstance.getName())
                .configFileRepositoryRequired(app.getAppConfigurationSpec().isConfigFileRepositoryRequired())
                .owner(principal.getName())
                .appName(app.getName())
                .descriptiveDeploymentId(createDescriptiveDeploymentId(domain.getCodename(), app.getName(), appInstance.getId()))
                .build();

        Identifier internalId = appLifecycleManager.deployApplication(appDeployment);
        appInstance.setInternalId(internalId);

        instances.update(appInstance);

        return new Id(appInstance.getId());
    }

    private Identifier createDescriptiveDeploymentId(String domain, String appName, Long appInstanceNumber) {
        return Identifier.newInstance(
                String.join("-", domain, appName.replace(" ", ""), String.valueOf(appInstanceNumber)).toLowerCase()
        );
    }

    @PostMapping("/{appInstanceId}/redeploy")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'CREATE')")
    @Transactional
    public void redeployAppInstance(@PathVariable Long appInstanceId) {
        try {
            AppInstance appInstance = getAppInstance(appInstanceId);
            this.appLifecycleManager.redeployApplication(appInstance.getInternalId());
        } catch (InvalidDeploymentIdException e) {
            throw new ProcessingException(MISSING_APP_INSTANCE_MESSAGE);
        }
    }

    @DeleteMapping("/{appInstanceId}")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'DELETE')")
    @Transactional
    public void deleteAppInstance(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                  @NotNull Principal principal) {
        try {
            AppInstance appInstance = getAppInstance(appInstanceId);
            appLifecycleManager.removeApplication(appInstance.getInternalId());
        } catch (InvalidDeploymentIdException e) {
            throw new ProcessingException(MISSING_APP_INSTANCE_MESSAGE);
        }
    }

    @DeleteMapping("/failed/{appInstanceId}")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'DELETE')")
    @Transactional
    public void removeFailedInstance(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                  @NotNull Principal principal) {
        try {
            AppInstance appInstance = getAppInstance(appInstanceId);
            appLifecycleManager.removeFailedApplication(appInstance.getInternalId());
        } catch (InvalidDeploymentIdException e) {
            throw new ProcessingException(MISSING_APP_INSTANCE_MESSAGE);
        }
    }

    @GetMapping("/{appInstanceId}/state")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public AppInstanceStatus getState(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                      @NotNull Principal principal) {
        AppInstance appInstance = getAppInstance(appInstanceId);
        return getAppInstanceState(appInstance);
    }

    @GetMapping("/{appInstanceId}/state/history")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public List<AppDeploymentHistoryView> getStateHistory(@PathVariable(value = "appInstanceId") Long appInstanceId, @NotNull Principal principal) {
        try {
            AppInstance appInstance = getAppInstance(appInstanceId);
            return appDeploymentMonitor.appDeploymentHistory(appInstance.getInternalId());
        } catch (InvalidDeploymentIdException e) {
            throw new MissingElementException(e.getMessage());
        }
    }

    @PostMapping("/{appInstanceId}/restart")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public void restartAppInstance(@PathVariable(value = "appInstanceId") Long appInstanceId) {
        try {
            AppInstance appInstance = getAppInstance(appInstanceId);
            this.appLifecycleManager.restartApplication(appInstance.getInternalId());
        } catch (InvalidDeploymentIdException e) {
            throw new ProcessingException(MISSING_APP_INSTANCE_MESSAGE);
        }
    }

    private AppInstanceStatus getAppInstanceState(AppInstance appInstance) {
        if (appInstance == null)
            throw new MissingElementException("App instance is null");

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
        AppInstanceState appInstanceState = mapAppInstanceState(state);

        return AppInstanceStatus.builder()
                .appInstanceId(appInstanceId)
                .details(state.name())
                .userFriendlyDetails(state.getUserFriendlyState())
                .state(appInstanceState)
                .previousState(mapAppInstanceState(previousState))
                .userFriendlyState(appInstanceState.getUserFriendlyState())
                .build();
    }

    private AppInstanceState mapAppInstanceState(AppLifecycleState state) {
        AppInstanceState appInstanceState;
        switch (state) {
            case REQUESTED:
                appInstanceState = AppInstanceState.REQUESTED;
                break;
            case REQUEST_VALIDATION_IN_PROGRESS:
            case REQUEST_VALIDATED:
                appInstanceState = AppInstanceState.VALIDATION;
                break;
            case DEPLOYMENT_ENVIRONMENT_PREPARATION_IN_PROGRESS:
                appInstanceState = AppInstanceState.PREPARATION;
                break;
            case DEPLOYMENT_ENVIRONMENT_PREPARED:
            case MANAGEMENT_VPN_CONFIGURATION_IN_PROGRESS:
                appInstanceState = AppInstanceState.CONNECTING;
                break;
            case MANAGEMENT_VPN_CONFIGURED:
            case APPLICATION_CONFIGURATION_IN_PROGRESS:
            case APPLICATION_CONFIGURED:
                appInstanceState = AppInstanceState.CONFIGURATION_AWAITING;
                break;
            case APPLICATION_DEPLOYMENT_IN_PROGRESS:
            case APPLICATION_DEPLOYED:
            case APPLICATION_CONFIGURATION_UPDATE_IN_PROGRESS:
            case APPLICATION_CONFIGURATION_UPDATED:
            case APPLICATION_RESTART_IN_PROGRESS:
            case APPLICATION_RESTARTED:
            case APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS:
                appInstanceState = AppInstanceState.DEPLOYING;
                break;
            case APPLICATION_DEPLOYMENT_VERIFIED:
                appInstanceState = AppInstanceState.RUNNING;
                break;
            case APPLICATION_REMOVAL_IN_PROGRESS:
                appInstanceState = AppInstanceState.UNDEPLOYING;
                break;
            case APPLICATION_REMOVED:
            case APPLICATION_CONFIGURATION_REMOVAL_IN_PROGRESS:
            case APPLICATION_CONFIGURATION_REMOVED:
                appInstanceState = AppInstanceState.DONE;
                break;
            case INTERNAL_ERROR:
            case REQUEST_VALIDATION_FAILED:
            case DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED:
            case MANAGEMENT_VPN_CONFIGURATION_FAILED:
            case APPLICATION_CONFIGURATION_FAILED:
            case APPLICATION_DEPLOYMENT_VERIFICATION_FAILED:
            case APPLICATION_REMOVAL_FAILED:
            case APPLICATION_RESTART_FAILED:
            case APPLICATION_CONFIGURATION_UPDATE_FAILED:
            case APPLICATION_DEPLOYMENT_FAILED:
            case APPLICATION_CONFIGURATION_REMOVAL_FAILED:
                appInstanceState = AppInstanceState.FAILURE;
                break;
            case FAILED_APPLICATION_REMOVED:
                appInstanceState = AppInstanceState.REMOVED;
                break;
            case UNKNOWN:
            default:
                appInstanceState = AppInstanceState.UNKNOWN;
                break;
        }
        return appInstanceState;
    }

    private AppInstance getAppInstance(Long appInstanceId) {
        if (appInstanceId == null)
            throw new MissingElementException("Missing app instance id.");
        return instances.find(appInstanceId).orElseThrow(() -> new MissingElementException("App instance not found."));
    }

    private AppInstanceView mapAppInstance(AppInstance appInstance) {
        if (appInstance == null)
            return null;
        AppInstanceView ai = modelMapper.map(appInstance, AppInstanceView.class);

        return this.addAppInstanceProperties(ai, appInstance);
    }

    private AppInstanceViewExtended mapAppInstanceExtended(AppInstance appInstance) {
        if (appInstance == null)
            return null;
        AppInstanceViewExtended ai = modelMapper.map(appInstance, AppInstanceViewExtended.class);

        return (AppInstanceViewExtended) addAppInstanceProperties(ai, appInstance);
    }

    private AppInstanceView addAppInstanceProperties(AppInstanceView ai, AppInstance appInstance) {

        try {
            ai.setState(mapAppInstanceState(this.appDeploymentMonitor.state(appInstance.getInternalId())));
            ai.setUserFriendlyState(ai.getState().getUserFriendlyState());
        } catch (Exception e) {
            ai.setState(AppInstanceState.UNKNOWN);
            ai.setUserFriendlyState(ai.getState().getUserFriendlyState());
        }

        if (!ai.getDomainId().equals(appInstance.getDomain().getId())) {
            ai.setDomainId(appInstance.getDomain().getId());
        }

        Identifier identifier = appInstance.getInternalId();
        try {
            ai.setServiceAccessMethods(this.appDeploymentMonitor.userAccessDetails(identifier).getServiceAccessMethods());
        } catch (InvalidAppStateException | InvalidDeploymentIdException e) {
            ai.setServiceAccessMethods(null);
        }

        try {
            ai.setAppConfigRepositoryAccessDetails(this.appDeploymentMonitor.configRepositoryAccessDetails(identifier));
        } catch (InvalidAppStateException | InvalidDeploymentIdException e) {
            ai.setAppConfigRepositoryAccessDetails(null);
        }

        try {
            ai.setDescriptiveDeploymentId(this.appDeploymentRepositoryManager.load(appInstance.getInternalId()).getDescriptiveDeploymentId().value());
        } catch (InvalidDeploymentIdException e) {
            ai.setDescriptiveDeploymentId(null);
        }

        try {
            ai.setConfigWizardTemplate(new ConfigWizardTemplateView(appInstance.getApplication().getConfigWizardTemplate().getTemplate()));
        } catch (Exception e) {
            ai.setConfigWizardTemplate(null);
        }

        try {
            ai.setConfigUpdateWizardTemplate(new ConfigWizardTemplateView(appInstance.getApplication().getConfigUpdateWizardTemplate().getTemplate()));
        } catch (Exception e) {
            ai.setConfigUpdateWizardTemplate(null);
        }

        return ai;
    }

    private void logPageable(Pageable p) {
        log.trace("Page number: " + p.getPageNumber() + "\tPage size:" +p.getPageSize() + "\tPage offset:" + p.getOffset() + "\tSort:" + p.getSort());
    }

    private boolean isPageableValidForAppInstance(Pageable p) {
        List<String> sortProperties = p.getSort().get().map(Sort.Order::getProperty).collect(Collectors.toList());
        List<String> classProperties = Arrays.stream(AppInstance.class.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());

        Set<String> sortSet = new HashSet<>(sortProperties);
        Set<String> classSet = new HashSet<>(classProperties);

        sortSet.removeAll(classSet);

        return sortSet.isEmpty();
    }

    private boolean isSystemAdminAndIsDomainGlobal(User user, Long domainId) {

        boolean isSystemAdmin = false;
        boolean isDomainGlobal = false;

        if(user.getRoles().stream().anyMatch((UserRole ur) -> ur.getRole().equals(Role.ROLE_SYSTEM_ADMIN))) {
            isSystemAdmin = true;
        }

        if(domainId.equals(domains.getGlobalDomain().orElseThrow(() -> new InvalidDomainException("Global Domain not found")).getId())) {
            isDomainGlobal =true;
        }

        return isSystemAdmin && isDomainGlobal;
    }

    private Pageable pageableValidator(Pageable pageable) {
        if(!this.isPageableValidForAppInstance(pageable)) {
            return null;
        }
        return pageable;
    }

}
