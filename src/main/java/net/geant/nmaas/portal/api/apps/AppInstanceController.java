package net.geant.nmaas.portal.api.apps;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.api.dto.Id;
import net.geant.nmaas.api.dto.applications.AppInstanceRequest;
import net.geant.nmaas.api.dto.applications.AppInstanceState;
import net.geant.nmaas.api.dto.users.UserBase;
import net.geant.nmaas.nmservice.configuration.gitlab.events.AddUserToRepositoryGitlabEvent;
import net.geant.nmaas.nmservice.configuration.gitlab.events.RemoveUserFromRepositoryGitlabEvent;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.AppScaleDirection;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.events.app.AppScaleActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.exceptions.ApplicationSubscriptionNotActiveException;
import net.geant.nmaas.portal.persistence.entity.AppInstance;
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.SSHKeyEntity;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/${nmaas.api.version:v1}/apps/instances")
@Slf4j
@Tag(name = "Application Instances", description = "Operations related to application instances")
public class AppInstanceController extends AppBaseController {

    private static final String MISSING_APP_INSTANCE_MESSAGE = "Missing app instance";

    private final AppLifecycleManager appLifecycleManager;
    private final AppDeploymentMonitor appDeploymentMonitor;
    private final ApplicationInstanceService instanceService;
    private final DomainService domainService;

    private final ConfigurationManager configurationManager;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${nmaas.platform.multi-instance}")
    private boolean useDeploymentPrefix;

    @Autowired
    public AppInstanceController(ModelMapper modelMapper,
                                 ApplicationService applicationService,
                                 ApplicationBaseService appBaseService,
                                 UserService userService,
                                 AppLifecycleManager appLifecycleManager,
                                 AppDeploymentMonitor appDeploymentMonitor,
                                 ApplicationInstanceService instanceService,
                                 DomainService domainService,
                                 ApplicationEventPublisher eventPublisher,
                                 ConfigurationManager configurationManager) {
        super(modelMapper, userService, applicationService, appBaseService);
        this.appLifecycleManager = appLifecycleManager;
        this.appDeploymentMonitor = appDeploymentMonitor;
        this.instanceService = instanceService;
        this.domainService = domainService;
        this.eventPublisher = eventPublisher;
        this.configurationManager = configurationManager;
    }

    @PostMapping("/domain/{domainId}")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
    @Transactional
    public Id createAppInstance(@RequestBody AppInstanceRequest appInstanceRequest,
                                @NotNull Principal principal,
                                @PathVariable Long domainId,
                                @RequestParam(name = "clusterId", required = false) Long clusterId) {
        log.info("Processing new application instance request");
        Application app = getApp(appInstanceRequest.applicationId());
        Domain domain = domainService.findDomain(domainId)
                .orElseThrow(() -> new MissingElementException("Domain not found"));
        log.info("for application {} in domain {} ({})",
                app.getName(),
                domain.getCodename(),
                Objects.isNull(clusterId) ? "central cluster" : "remote cluster: " + clusterId);
        verifyName(appInstanceRequest, domain);

        AppInstance appInstance;
        try {
            appInstance = instanceService.create(domain, app, appInstanceRequest.name(), appInstanceRequest.autoUpgradesEnabled());
            if (Objects.nonNull(clusterId)) {
                appInstance.setRemoteClusterId(clusterId);
            }
        } catch (ApplicationSubscriptionNotActiveException e) {
            throw new ProcessingException("Unable to create instance: " + e.getMessage());
        }

        AppDeployment appDeployment = AppDeployment.builder()
                .domain(domain.getCodename())
                .instanceId(appInstance.getId())
                .applicationId(Identifier.newInstance(appInstance.getApplication().getId()))
                .deploymentName(appInstance.getName())
                .configFileRepositoryRequired(app.getAppConfigurationSpec().isConfigFileRepositoryRequired())
                .configUpdateEnabled(app.getAppConfigurationSpec().isConfigUpdateEnabled())
                .termsAcceptanceRequired(app.getAppConfigurationSpec().isTermsAcceptanceRequired())
                .owner(principal.getName())
                .appName(app.getName())
                .descriptiveDeploymentId(createDescriptiveDeploymentId(domain.getCodename(), app.getName(), appInstance.getId()))
                .remoteClusterId(clusterId)
                .build();

        Identifier internalId = appLifecycleManager.deployApplication(appDeployment, principal.getName());
        appInstance.setInternalId(internalId);

        instanceService.update(appInstance);

        return new Id(appInstance.getId());
    }

    private void verifyName(AppInstanceRequest appInstanceRequest, Domain domain) {
        /*
        check name uniqueness
        forbidden names - names of all app instances in domain, where state is different from 'DONE' and 'REMOVED'
        */
        Set<String> forbiddenNames = instanceService.findAllByDomain(domain).stream() // get all app instances in domain
                .filter(appInst -> {
                    // map their internal state to app instance state
                    AppInstanceState state = mapAppInstanceState(appDeploymentMonitor.state(appInst.getInternalId()));
                    // check if it does not equal 'DONE' or 'REMOVED'
                    return !(state.equals(AppInstanceState.DONE) || state.equals(AppInstanceState.REMOVED));
                })
                .map(AppInstance::getName) // take names only
                .map(String::toLowerCase) // set all names to lower case
                .collect(Collectors.toSet());
        if (forbiddenNames.contains(appInstanceRequest.name().toLowerCase())) {
            throw new IllegalArgumentException("Name is already taken");
        }
    }

    public Identifier createDescriptiveDeploymentId(String domain, String appName, Long appInstanceNumber) {
        if (useDeploymentPrefix) {
            return Identifier.newInstance(
                    String.join("-", configurationManager.getConfiguration().getDeploymentPrefix(),
                            domain,
                            appName.replace(" ", ""),
                            String.valueOf(appInstanceNumber)).toLowerCase());
        } else {
            return Identifier.newInstance(
                    String.join("-", domain, appName.replace(" ", ""), String.valueOf(appInstanceNumber)).toLowerCase()
            );
        }
    }

    @DeleteMapping("/{appInstanceId}")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'DELETE')")
    @Transactional
    public void deleteAppInstance(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                  @NotNull Principal principal
    ) {
        try {
            AppInstance appInstance = getAppInstance(appInstanceId);
            appLifecycleManager.removeApplication(appInstance.getInternalId(), principal.getName());
        } catch (InvalidDeploymentIdException e) {
            throw new ProcessingException(MISSING_APP_INSTANCE_MESSAGE);
        }
    }

    @DeleteMapping("/failed/{appInstanceId}")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'DELETE')")
    @Transactional
    public void removeFailedInstance(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                     @NotNull Principal principal
    ) {
        try {
            AppInstance appInstance = getAppInstance(appInstanceId);
            appLifecycleManager.removeFailedApplication(appInstance.getInternalId(), principal.getName());
        } catch (InvalidDeploymentIdException e) {
            throw new ProcessingException(MISSING_APP_INSTANCE_MESSAGE);
        }
    }

    @PostMapping("/{appInstanceId}/restart")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public void restartAppInstance(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                   @NotNull Principal principal
    ) {
        try {
            AppInstance appInstance = getAppInstance(appInstanceId);
            this.appLifecycleManager.restartApplication(appInstance.getInternalId(), principal.getName());
        } catch (InvalidDeploymentIdException e) {
            throw new ProcessingException(MISSING_APP_INSTANCE_MESSAGE);
        }
    }

    @PostMapping("/{appInstanceId}/redeploy")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public void redeployAppInstance(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                    @NotNull Principal principal
    ) {
        try {
            AppInstance appInstance = getAppInstance(appInstanceId);
            this.appLifecycleManager.redeployApplication(appInstance.getInternalId(), principal.getName());
        } catch (InvalidDeploymentIdException e) {
            throw new ProcessingException(MISSING_APP_INSTANCE_MESSAGE);
        }
    }

    @PostMapping("/{appInstanceId}/upgrade/{targetApplicationId}")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public void upgradeAppInstance(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                   @PathVariable(value = "targetApplicationId") Long targetApplicationId,
                                   @NotNull Principal principal) {
        try {
            AppInstance appInstance = getAppInstance(appInstanceId);
            this.appLifecycleManager.upgradeApplication(
                    appInstance.getInternalId(),
                    Identifier.newInstance(targetApplicationId),
                    principal.getName()
            );
        } catch (InvalidDeploymentIdException e) {
            throw new ProcessingException(MISSING_APP_INSTANCE_MESSAGE);
        }
    }

    @PostMapping("/{appInstanceId}/enableAutoUpgrades")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public void enableAutoUpgradesForAppInstance(@PathVariable(value = "appInstanceId") Long appInstanceId) {
        try {
            AppInstance appInstance = getAppInstance(appInstanceId);
            appInstance.setAutoUpgradesEnabled(true);
            this.instanceService.update(appInstance);
        } catch (InvalidDeploymentIdException e) {
            throw new ProcessingException(MISSING_APP_INSTANCE_MESSAGE);
        }
    }

    @PostMapping("/{appInstanceId}/disableAutoUpgrades")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public void disableAutoUpgradesForAppInstance(@PathVariable(value = "appInstanceId") Long appInstanceId) {
        try {
            AppInstance appInstance = getAppInstance(appInstanceId);
            appInstance.setAutoUpgradesEnabled(false);
            this.instanceService.update(appInstance);
        } catch (InvalidDeploymentIdException e) {
            throw new ProcessingException(MISSING_APP_INSTANCE_MESSAGE);
        }
    }

    @PostMapping("/{appInstanceId}/check")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    public void checkStatus(@PathVariable(value = "appInstanceId") Long appInstanceId) {
        try {
            AppInstance appInstance = getAppInstance(appInstanceId);
            appLifecycleManager.updateApplicationStatus(appInstance.getInternalId());
        } catch (InvalidDeploymentIdException e) {
            throw new ProcessingException(MISSING_APP_INSTANCE_MESSAGE);
        }
    }

    @PostMapping("/{appInstanceId}/version/{version}")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    public void changeManualVersion(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                    @PathVariable(value = "version") String version) {
        try {
            AppInstance appInstance = getAppInstance(appInstanceId);
            Application application = getApp(appInstance.getApplication().getName(), version);
            appInstance.setApplication(application);
            this.instanceService.update(appInstance);
        } catch (InvalidDeploymentIdException e) {
            throw new ProcessingException(MISSING_APP_INSTANCE_MESSAGE);
        }
    }

    @PostMapping("/{appInstanceId}/members")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    public void updateMembers(@PathVariable(value = "appInstanceId") Long appInstanceId, @RequestBody @Valid List<UserBase> members) {
        AppInstance appInstance = getAppInstance(appInstanceId);
        Set<User> oldMembers = new HashSet<>(appInstance.getMembers()); // copy members set

        Set<String> oldMemberUsernames = appInstance.getMembers().stream().map(User::getUsername).collect(Collectors.toSet());
        Set<String> newMemberUsernames = members.stream().map(UserBase::getUsername).collect(Collectors.toSet());

        Set<String> commonMemberUsernames = new HashSet<>(oldMemberUsernames);
        commonMemberUsernames.retainAll(newMemberUsernames); // retrieve intersection of old and new members - these users won't be affected

        Set<String> toRemoveMemberUsernames = new HashSet<>(oldMemberUsernames);
        toRemoveMemberUsernames.removeAll(commonMemberUsernames); // get usernames to be removed from members list

        Set<String> toAddMemberUsernames = new HashSet<>(newMemberUsernames);
        toAddMemberUsernames.removeAll(commonMemberUsernames); // get usernames to be added to members list

        // retrieve users from usernames to be added to members
        List<User> usersToAdd = toAddMemberUsernames.stream()
                .map(this::getUser)
                .filter(u -> !u.getSshKeys().isEmpty()) // skip users with no ssh keys
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getDomain().getId().equals(appInstance.getDomain().getId()))) // allow only users with role in app instance domain
                .toList();

        // get user data to be removed from members
        List<User> usersToRemove = oldMembers.stream()
                .filter(m -> toRemoveMemberUsernames.contains(m.getUsername()))
                .toList();

        // update list of members in the database
        usersToRemove.forEach(appInstance.getMembers()::remove);
        appInstance.getMembers().addAll(new HashSet<>(usersToAdd));
        this.instanceService.update(appInstance);

        usersToRemove.forEach(r -> {
            RemoveUserFromRepositoryGitlabEvent event = new RemoveUserFromRepositoryGitlabEvent(
                    "AppInstance members list update",
                    r.getUsername(),
                    appInstance.getInternalId()
            );
            eventPublisher.publishEvent(event);
        });

        usersToAdd.forEach(a -> {
            if (a.getSshKeys().isEmpty()) {
                log.info("[ADD USER TO GITLAB REPO] User [{}] does not have any ssh keys, skipping", a.getUsername());
            } else {
                AddUserToRepositoryGitlabEvent event = new AddUserToRepositoryGitlabEvent(
                        "AppInstance members list update",
                        a.getUsername(),
                        a.getEmail(),
                        a.getFirstname() + " " + a.getLastname(),
                        a.getSshKeys().stream().map(SSHKeyEntity::getKeyValue).toList(),
                        appInstance.getInternalId()
                );
                eventPublisher.publishEvent(event);
            }
        });
    }

    public static AppInstanceState mapAppInstanceState(AppLifecycleState state) {
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
            case APPLICATION_CONFIGURATION_UPDATE_IN_PROGRESS:
            case APPLICATION_CONFIGURATION_UPDATED:
            case APPLICATION_RESTART_IN_PROGRESS:
            case APPLICATION_RESTARTED:
            case APPLICATION_PAUSE_IN_PROGRESS:
            case APPLICATION_UPGRADE_IN_PROGRESS:
            case APPLICATION_UPGRADED:
            case APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS:
                appInstanceState = AppInstanceState.DEPLOYING;
                break;
            case APPLICATION_DEPLOYMENT_VERIFIED:
                appInstanceState = AppInstanceState.RUNNING;
                break;
            case APPLICATION_PAUSED:
                appInstanceState = AppInstanceState.PAUSED;
                break;
            case APPLICATION_REMOVAL_IN_PROGRESS:
                appInstanceState = AppInstanceState.UNDEPLOYING;
                break;
            case APPLICATION_REMOVED:
            case APPLICATION_CONFIGURATION_REMOVAL_IN_PROGRESS:
            case APPLICATION_CONFIGURATION_REMOVED:
                appInstanceState = AppInstanceState.DONE;
                break;
            case REQUEST_VALIDATION_FAILED:
                appInstanceState = AppInstanceState.VALIDATION_FAILED;
                break;
            case INTERNAL_ERROR:
            case DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED:
            case MANAGEMENT_VPN_CONFIGURATION_FAILED:
            case APPLICATION_CONFIGURATION_FAILED:
            case APPLICATION_DEPLOYMENT_VERIFICATION_FAILED:
            case APPLICATION_REMOVAL_FAILED:
            case APPLICATION_RESTART_FAILED:
            case APPLICATION_PAUSE_FAILED:
            case APPLICATION_CONFIGURATION_UPDATE_FAILED:
            case APPLICATION_DEPLOYMENT_FAILED:
            case APPLICATION_CONFIGURATION_REMOVAL_FAILED:
            case APPLICATION_UPGRADE_FAILED:
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
        if (appInstanceId == null) {
            throw new MissingElementException("Missing app instance identifier");
        }
        return instanceService.find(appInstanceId).orElseThrow(() -> new MissingElementException("App instance not found."));
    }

    @PutMapping("/{appInstanceId}/scale-down")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void scaleDownAppInstance(@PathVariable Long appInstanceId,
                                     @NotNull Principal principal
    ) {
        final AppInstance appInstance = getAppInstance(appInstanceId);
        final Identifier deploymentId = appInstance.getInternalId();
        if (appDeploymentMonitor.state(deploymentId).equals(AppLifecycleState.APPLICATION_PAUSED)) {
            log.warn("Won't pause since application instance is already paused");
            return;
        }
        eventPublisher.publishEvent(
                new AppScaleActionEvent(
                        this,
                        deploymentId,
                        AppScaleDirection.DOWN,
                        principal.getName())
        );
    }

    @PutMapping("/{appInstanceId}/scale-up")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void scaleUpAppInstance(@PathVariable Long appInstanceId,
                                   @NotNull Principal principal
    ) {
        final AppInstance appInstance = getAppInstance(appInstanceId);
        final Identifier deploymentId = appInstance.getInternalId();
        if (appDeploymentMonitor.state(deploymentId).equals(AppLifecycleState.APPLICATION_PAUSED)) {
            eventPublisher.publishEvent(
                    new AppScaleActionEvent(
                            this,
                            deploymentId,
                            AppScaleDirection.UP,
                            principal.getName())
            );
        } else {
            log.warn("Won't resume since application instance is not paused");
        }
    }

}
