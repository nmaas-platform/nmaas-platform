package net.geant.nmaas.portal.api.market;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.api.model.AppConfigurationView;
import net.geant.nmaas.orchestration.api.model.AppDeploymentHistoryView;
import net.geant.nmaas.orchestration.entities.AppLifecycleState;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.api.domain.AppDeploymentSpec;
import net.geant.nmaas.portal.api.domain.AppInstance;
import net.geant.nmaas.portal.api.domain.AppInstanceState;
import net.geant.nmaas.portal.api.domain.AppInstanceStatus;
import net.geant.nmaas.portal.api.domain.AppInstanceSubscription;
import net.geant.nmaas.portal.api.domain.Id;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.api.model.EmailConfirmation;
import net.geant.nmaas.portal.exceptions.ApplicationSubscriptionNotActiveException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.NotificationService;
import net.geant.nmaas.portal.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AppInstanceController extends AppBaseController {

    private AppLifecycleManager appLifecycleManager;

    private AppDeploymentMonitor appDeploymentMonitor;

    private ApplicationInstanceService instances;

    private DomainService domains;

    private static final String MISSING_APP_INSTANCE_MESSAGE = "Missing app instance";

    private NotificationService notificationService;

    private UserService userService;

    @Autowired
    public AppInstanceController(AppLifecycleManager appLifecycleManager, AppDeploymentMonitor appDeploymentMonitor,
                                 ApplicationInstanceService applicationInstanceService, DomainService domains,
                                 NotificationService notificationService,
                                 UserService userService) {
        this.appLifecycleManager = appLifecycleManager;
        this.appDeploymentMonitor = appDeploymentMonitor;
        this.instances = applicationInstanceService;
        this.domains = domains;
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping("/apps/instances")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @Transactional
    public List<AppInstance> getAllInstances(Pageable pageable) throws MissingElementException {
        return instances.findAll(pageable).getContent().stream().map(appInstance -> mapAppInstance(appInstance)).collect(Collectors.toList());
    }

    @GetMapping("/apps/instances/my")
    @Transactional
    public List<AppInstance> getMyAllInstances(@NotNull Principal principal, Pageable pageable) throws MissingElementException {
        User user = users.findByUsername(principal.getName()).orElseThrow(() -> new MissingElementException("User not found"));

        return instances.findAllByOwner(user, pageable).getContent().stream().map(appInstance -> mapAppInstance(appInstance)).collect(Collectors.toList());
    }

    @GetMapping("/domains/{domainId}/apps/instances")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'ANY')")
    @Transactional
    public List<AppInstance> getAllInstances(@PathVariable Long domainId, Pageable pageable) throws MissingElementException {
        net.geant.nmaas.portal.persistent.entity.Domain domain = domains.findDomain(domainId).orElseThrow(() -> new MissingElementException("Domain " + domainId + " not found"));

        return instances.findAllByDomain(domain, pageable).getContent().stream().map(appInstance -> mapAppInstance(appInstance)).collect(Collectors.toList());
    }

    @GetMapping(value = "/domains/{domainId}/apps/instances/my")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'ANY')")
    @Transactional
    public List<AppInstance> getMyAllInstances(@PathVariable Long domainId, @NotNull Principal principal, Pageable pageable)
            throws MissingElementException {
        return getUserDomainAppInstances(domainId, principal.getName(), pageable);
    }

    @GetMapping("/domains/{domainId}/apps/instances/user/{username}")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
    @Transactional
    public List<AppInstance> getUserAllInstances(@PathVariable Long domainId, @PathVariable String username, Pageable pageable)
            throws MissingElementException {
        return getUserDomainAppInstances(domainId, username, pageable);
    }

    private List<AppInstance> getUserDomainAppInstances(Long domainId, String username, Pageable pageable) throws MissingElementException {

        net.geant.nmaas.portal.persistent.entity.Domain domain = domains.findDomain(domainId)
                .orElseThrow(() -> new MissingElementException("Domain " + domainId + " not found"));

        User user = users.findByUsername(username)
                .orElseThrow(() -> new MissingElementException("User not found"));

        return instances.findAllByOwner(user, domain, pageable).getContent().stream().map(appInstance -> mapAppInstance(appInstance)).collect(Collectors.toList());
    }

    @GetMapping({"/apps/instances/{appInstanceId}", "/domains/{domainId}/apps/instances/{appInstanceId}"})
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public AppInstance getAppInstance(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                      @NotNull Principal principal) throws MissingElementException, ProcessingException {
        net.geant.nmaas.portal.persistent.entity.AppInstance appInstance = instances.find(appInstanceId).orElseThrow(() -> new MissingElementException("App instance not found."));

        return mapAppInstance(appInstance);
    }

    @PostMapping("/domains/{domainId}/apps/instances")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'CREATE')")
    @Transactional
    public Id createAppInstance(@RequestBody(required = true) AppInstanceSubscription appInstanceSubscription,
                                @NotNull Principal principal, @PathVariable Long domainId) throws MissingElementException, ProcessingException {
        Application app = getApp(appInstanceSubscription.getApplicationId());

        net.geant.nmaas.portal.persistent.entity.Domain domain = domains.findDomain(domainId).orElseThrow(() -> new MissingElementException("Domain not found"));

        net.geant.nmaas.portal.persistent.entity.AppInstance appInstance;
        try {
            appInstance = instances.create(domain, app, appInstanceSubscription.getName());
        } catch (ApplicationSubscriptionNotActiveException e) {
            throw new ProcessingException("Unable to create instance. " + e.getMessage());
        }

        Identifier internalId = appLifecycleManager.deployApplication(
                domain.getCodename(),
                Identifier.newInstance(appInstance.getApplication().getId()),
                appInstance.getName(),
                modelMapper.map(app.getAppDeploymentSpec(), AppDeploymentSpec.class));
        appInstance.setInternalId(internalId);
        final User user = userService.findByUsername(principal.getName()).orElseThrow(ProcessingException::new);
        final List<User> domainUsers = domains.findUsersWithDomainAdminRole(domainId);

        notificationService.sendEmail(getAppInstanceReadyEmailConfirmation(user, appInstance.getName(), app.getName(), domain.getCodename()));
        domainUsers.forEach(domainUser ->
                notificationService.sendEmail(getDomainAdminNotificationEmailConfirmation(domainUser, appInstance.getName(), app.getName(), domain.getCodename())));

        instances.update(appInstance);
        return new Id(appInstance.getId());
    }

    @PostMapping({"/apps/instances/{appInstanceId}/redeploy", "/domains/{domainId}/apps/instances/{appInstanceId}/redeploy"})
    @PreAuthorize("hasPermission(#domainId, 'domain', 'CREATE')")
    @Transactional
    public void redeployAppInstance(@PathVariable Long appInstanceId) {
        net.geant.nmaas.portal.persistent.entity.AppInstance appInstance = getAppInstance(appInstanceId);
        try {
            this.appLifecycleManager.redeployApplication(appInstance.getInternalId());
        } catch (InvalidDeploymentIdException e) {
            throw new ProcessingException(MISSING_APP_INSTANCE_MESSAGE);
        }
    }

    @DeleteMapping({"/apps/instances/{appInstanceId}", "/domains/{domainId}/apps/instances/{appInstanceId}"})
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'DELETE')")
    @Transactional
    public void deleteAppInstance(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                  @NotNull Principal principal) throws MissingElementException, ProcessingException {
        net.geant.nmaas.portal.persistent.entity.AppInstance appInstance = getAppInstance(appInstanceId);

        try {
            appLifecycleManager.removeApplication(appInstance.getInternalId());
        } catch (InvalidDeploymentIdException e) {
            throw new ProcessingException(MISSING_APP_INSTANCE_MESSAGE);
        }
    }

    @PostMapping({"/apps/instances/{appInstanceId}/configure", "/domains/{domainId}/apps/instances/{appInstanceId}/configure"})
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public void applyConfiguration(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                   @RequestBody AppConfigurationView configuration, @NotNull Principal principal)
            throws MissingElementException, ProcessingException {
        net.geant.nmaas.portal.persistent.entity.AppInstance appInstance = getAppInstance(appInstanceId);

        boolean valid = validJSON(configuration.getJsonInput());
        if (!valid)
            throw new ProcessingException("Configuration is not in valid JSON format");

		if(configuration.getStorageSpace() != null && configuration.getStorageSpace() <= 0)
			throw new ProcessingException("Storage space cannot be less or equal 0");

		appInstance.setConfiguration(configuration.getJsonInput());
		instances.update(appInstance);

		try {
			appLifecycleManager.applyConfiguration(appInstance.getInternalId(), configuration);
		} catch (Throwable e) {
			throw new ProcessingException(MISSING_APP_INSTANCE_MESSAGE);
		}
	}

    @GetMapping({"/apps/instances/{appInstanceId}/state", "/domains/{domainId}/apps/instances/{appInstanceId}/state"})
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public AppInstanceStatus getState(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                      @NotNull Principal principal) throws MissingElementException, ProcessingException {
        net.geant.nmaas.portal.persistent.entity.AppInstance appInstance = getAppInstance(appInstanceId);

        return getAppInstanceState(appInstance);
    }

    @GetMapping({"/apps/instances/{appInstanceId}/state/history", "/domains/{domainId}/apps/instances/{appInstanceId}/state/history"})
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public List<AppDeploymentHistoryView> getStateHistory(@PathVariable(value = "appInstanceId") Long appInstanceId, @NotNull Principal principal) throws MissingElementException {
        try {
            net.geant.nmaas.portal.persistent.entity.AppInstance appInstance = getAppInstance(appInstanceId);
            return appDeploymentMonitor.appDeploymentHistory(appInstance.getInternalId());
        } catch (InvalidDeploymentIdException e) {
            throw new MissingElementException(e.getMessage());
        }
    }

    //domainId is not used in this method.
    @PostMapping({"/apps/instances/{appInstanceId}/restart", "/domains/{domainId}/apps/instances/{appInstanceId}/restart"})
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public void restartAppInstance(@PathVariable(value = "appInstanceId") Long appInstanceId) throws MissingElementException, ProcessingException {
        net.geant.nmaas.portal.persistent.entity.AppInstance appInstance = getAppInstance(appInstanceId);
        try {
            this.appLifecycleManager.restartApplication(appInstance.getInternalId());
        } catch (InvalidDeploymentIdException e) {
            throw new ProcessingException(MISSING_APP_INSTANCE_MESSAGE);
        }
    }

    private AppInstanceStatus getAppInstanceState(net.geant.nmaas.portal.persistent.entity.AppInstance appInstance) throws ProcessingException, MissingElementException {
        if (appInstance == null)
            throw new MissingElementException("App instance is null");

        AppLifecycleState state = AppLifecycleState.UNKNOWN;
        AppLifecycleState previousState = AppLifecycleState.UNKNOWN;
        try {
            state = appDeploymentMonitor.state(appInstance.getInternalId());
            previousState = appDeploymentMonitor.previousState(appInstance.getInternalId());
        } catch (InvalidDeploymentIdException e) {
            throw new ProcessingException(MISSING_APP_INSTANCE_MESSAGE);
        }

        return prepareAppInstanceStatus(appInstance.getId(), state, previousState);
    }

    private boolean validJSON(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private AppInstanceStatus prepareAppInstanceStatus(Long appInstanceId, AppLifecycleState state, AppLifecycleState previousState) {
        AppInstanceStatus appInstanceStatus = new AppInstanceStatus();
        appInstanceStatus.setAppInstanceId(appInstanceId);
        appInstanceStatus.setDetails(state.name());
        appInstanceStatus.setUserFriendlyDetails(state.getUserFriendlyState());

        AppInstanceState appInstanceState = mapAppInstanceState(state);

        appInstanceStatus.setState(appInstanceState);
        appInstanceStatus.setPreviousState(mapAppInstanceState(previousState));
        appInstanceStatus.setUserFriendlyState(appInstanceState.getUserFriendlyState());

        return appInstanceStatus;
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
                appInstanceState = AppInstanceState.CONFIGURATION_AWAITING;
                break;
            case APPLICATION_CONFIGURATION_IN_PROGRESS:
            case APPLICATION_CONFIGURED:
            case APPLICATION_DEPLOYMENT_IN_PROGRESS:
            case APPLICATION_DEPLOYED:
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
                appInstanceState = AppInstanceState.DONE;
                break;
            case INTERNAL_ERROR:
            case GENERIC_ERROR:
            case REQUEST_VALIDATION_FAILED:
            case DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED:
            case MANAGEMENT_VPN_CONFIGURATION_FAILED:
            case APPLICATION_CONFIGURATION_FAILED:
            case APPLICATION_DEPLOYMENT_VERIFICATION_FAILED:
            case APPLICATION_REMOVAL_FAILED:
            case APPLICATION_DEPLOYMENT_FAILED:
                appInstanceState = AppInstanceState.FAILURE;
                break;
            case UNKNOWN:
            default:
                appInstanceState = AppInstanceState.UNKNOWN;
                break;
        }
        return appInstanceState;
    }

    private net.geant.nmaas.portal.persistent.entity.AppInstance getAppInstance(Long appInstanceId)
            throws MissingElementException {
        if (appInstanceId == null)
            throw new MissingElementException("Missing app instance id.");
        return instances.find(appInstanceId).orElseThrow(() -> new MissingElementException("App instance not found."));
    }

    private AppInstance mapAppInstance(net.geant.nmaas.portal.persistent.entity.AppInstance appInstance) {
        if (appInstance == null)
            return null;
        AppInstance ai = modelMapper.map(appInstance, AppInstance.class);

        try {
            ai.setState(mapAppInstanceState(this.appDeploymentMonitor.state(appInstance.getInternalId())));
            ai.setUserFriendlyState(ai.getState().getUserFriendlyState());
            ai.setDomainId(appInstance.getDomain().getId());
        } catch (Exception e) {
            ai.setState(AppInstanceState.UNKNOWN);
            ai.setUrl(null);
        }

        try {
            ai.setUrl(this.appDeploymentMonitor.userAccessDetails(appInstance.getInternalId()).getUrl());
        } catch (InvalidAppStateException
                | InvalidDeploymentIdException e) {
            ai.setUrl(null);
        }

        return ai;
    }

    private EmailConfirmation getAppInstanceReadyEmailConfirmation(User user, String appInstanceName, String appName, String domainName){
        return EmailConfirmation.builder()
                .toEmail(user.getEmail())
                .firstName(Optional.ofNullable(user.getFirstname()).orElse(user.getUsername()))
                .lastName(user.getLastname())
                .appInstanceName(appInstanceName)
                .appName(appName)
                .domainName(domainName)
                .subject("Your app instance is ready")
                .templateName("app-instance-ready-notification")
                .build();
    }

    private EmailConfirmation getDomainAdminNotificationEmailConfirmation(User user, String appInstanceName, String appName, String domainName){
        return EmailConfirmation.builder()
                .toEmail(user.getEmail())
                .firstName(Optional.ofNullable(user.getFirstname()).orElse(user.getUsername()))
                .lastName(user.getLastname())
                .appInstanceName(appInstanceName)
                .appName(appName)
                .domainName(domainName)
                .subject("New app instance is ready")
                .templateName("domain-admin-notification")
                .build();
    }
}
