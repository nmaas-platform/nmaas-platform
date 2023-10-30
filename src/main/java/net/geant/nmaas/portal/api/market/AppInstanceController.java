package net.geant.nmaas.portal.api.market;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.configuration.gitlab.events.AddUserToRepositoryGitlabEvent;
import net.geant.nmaas.nmservice.configuration.gitlab.events.RemoveUserFromRepositoryGitlabEvent;
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
import net.geant.nmaas.portal.api.domain.AppInstanceBase;
import net.geant.nmaas.portal.api.domain.AppInstanceRequest;
import net.geant.nmaas.portal.api.domain.AppInstanceState;
import net.geant.nmaas.portal.api.domain.AppInstanceStatus;
import net.geant.nmaas.portal.api.domain.AppInstanceView;
import net.geant.nmaas.portal.api.domain.AppInstanceViewExtended;
import net.geant.nmaas.portal.api.domain.ApplicationBaseView;
import net.geant.nmaas.portal.api.domain.ConfigWizardTemplateView;
import net.geant.nmaas.portal.api.domain.Id;
import net.geant.nmaas.portal.api.domain.UserBase;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.exceptions.ApplicationSubscriptionNotActiveException;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.SSHKeyEntity;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/apps/instances")
@AllArgsConstructor
@Log4j2
public class AppInstanceController extends AppBaseController {

    private static final String MISSING_APP_INSTANCE_MESSAGE = "Missing app instance";
    private static final String MISSING_USER_MESSAGE = "User not found";

    private final AppLifecycleManager appLifecycleManager;
    private final AppDeploymentMonitor appDeploymentMonitor;
    private final ApplicationInstanceService instanceService;
    private final DomainService domainService;
    private final AppDeploymentRepositoryManager appDeploymentRepositoryManager;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public AppInstanceController(ModelMapper modelMapper,
                                 ApplicationService applicationService,
                                 ApplicationBaseService appBaseService,
                                 UserService userService,
                                 AppLifecycleManager appLifecycleManager,
                                 AppDeploymentMonitor appDeploymentMonitor,
                                 ApplicationInstanceService instanceService,
                                 DomainService domainService,
                                 AppDeploymentRepositoryManager appDeploymentRepositoryManager,
                                 ApplicationEventPublisher eventPublisher) {
        super(modelMapper, applicationService, appBaseService, userService);
        this.appLifecycleManager = appLifecycleManager;
        this.appDeploymentMonitor = appDeploymentMonitor;
        this.instanceService = instanceService;
        this.domainService = domainService;
        this.appDeploymentRepositoryManager = appDeploymentRepositoryManager;
        this.eventPublisher = eventPublisher;
    }

    /*
    NOTICE:
    NMAAS-756
    temporary fix on pagination size issue involves changing default pagination size in application.properties
    to mitigate this issue in the future, it is advised to implement server-side pagination,
    currently both api and user interface does not support this feature
     */

    @GetMapping
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @Transactional
    public List<AppInstanceBase> getAllInstances(Pageable pageable) {
        this.logPageable(pageable);
        pageable = this.pageableValidator(pageable);
        List<AppInstance> source = pageable == null ? instanceService.findAll() : instanceService.findAll(pageable).getContent();
        return source.stream()
                .map(this::mapAppInstanceBase)
                .collect(Collectors.toList());
    }

    @GetMapping("/my")
    @Transactional
    public List<AppInstanceBase> getMyAllInstances(@NotNull Principal principal, Pageable pageable) {
        this.logPageable(pageable);
        pageable = this.pageableValidator(pageable);
        User user = userService.findByUsername(principal.getName()).orElseThrow(() -> new MissingElementException(MISSING_USER_MESSAGE));
        return instanceService.findAllByOwner(user, pageable).getContent().stream()
                .map(this::mapAppInstanceBase)
                .collect(Collectors.toList());
    }

    @GetMapping("/domain/{domainId}")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'ANY')")
    @Transactional
    public List<AppInstanceBase> getAllInstances(@PathVariable Long domainId, @NotNull Principal principal, Pageable pageable) {
        this.logPageable(pageable);
        pageable = this.pageableValidator(pageable);
        Domain domain = domainService.findDomain(domainId)
                .orElseThrow(() -> new MissingElementException("Domain " + domainId + " not found"));
        User user = this.userService.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException(MISSING_USER_MESSAGE));

        // system admin on global view has an overall view over all instances
        if (this.isSystemAdminAndIsDomainGlobal(user, domainId)) {
            List<AppInstance> source = pageable == null ? instanceService.findAll() : instanceService.findAll(pageable).getContent();
            return source.stream()
                    .map(this::mapAppInstanceBase)
                    .collect(Collectors.toList());
        } else {
            return instanceService.findAllByDomain(domain, pageable).getContent().stream()
                    .map(this::mapAppInstanceBase)
                    .collect(Collectors.toList());
        }
    }

    @GetMapping("/running/domain/{domainId}")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'ANY')")
    @Transactional
    public List<AppInstanceView> getRunningAppInstances(@PathVariable(value = "domainId") long domainId,
                                                        @NotNull Principal principal) {
        Domain domain = this.domainService.findDomain(domainId).orElseThrow(() -> new InvalidDomainException("Domain not found"));
        return getAllRunningByDomain(domain);
    }

    @GetMapping("/running/app/{id}")
    @Transactional
    public boolean hasRunningInstance(@PathVariable Long id) {
        ApplicationBase appBase = appBaseService.getBaseApp(id);
        return appBase.getVersions().stream()
                .map(version -> applicationService.findApplication(version.getAppVersionId())
                        .orElseThrow(() -> new RuntimeException("Application not found")))
                .map(instanceService::findAllByApplication)
                .flatMap(List::stream)
                .anyMatch(this::isInstanceRunning);
    }

    private List<AppInstanceView> getAllRunningByDomain(Domain domain) {
        return this.instanceService.findAllByDomain(domain).stream()
                .filter(this::isInstanceRunning)
                .map(this::mapAppInstance)
                .collect(Collectors.toList());
    }

    private boolean isInstanceRunning(AppInstance app) {
        return appDeploymentMonitor.state(app.getInternalId()).equals(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED);
    }

    @GetMapping(value = "/domain/{domainId}/my")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'ANY')")
    @Transactional
    public List<AppInstanceBase> getMyAllInstances(@PathVariable Long domainId,
                                                   @NotNull Principal principal,
                                                   Pageable pageable) {
        this.logPageable(pageable);
        pageable = this.pageableValidator(pageable);
        User user = this.userService.findByUsername(principal.getName()).orElseThrow(() -> new UsernameNotFoundException(MISSING_USER_MESSAGE));

        if (this.isSystemAdminAndIsDomainGlobal(user, domainId)) {
            return instanceService.findAllByOwner(user, pageable).getContent().stream()
                    .map(this::mapAppInstanceBase)
                    .collect(Collectors.toList());
        } else {
            return getUserDomainAppInstances(domainId, principal.getName(), pageable);
        }
    }

    @GetMapping("/domain/{domainId}/user/{username}")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
    @Transactional
    public List<AppInstanceBase> getUserAllInstances(@PathVariable Long domainId,
                                                     @PathVariable String username,
                                                     Pageable pageable){
        this.logPageable(pageable);
        pageable = this.pageableValidator(pageable);
        return getUserDomainAppInstances(domainId, username, pageable);
    }

    private List<AppInstanceBase> getUserDomainAppInstances(Long domainId, String username, Pageable pageable) {
        Domain domain = domainService.findDomain(domainId)
                .orElseThrow(() -> new MissingElementException("Domain " + domainId + " not found"));
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new MissingElementException(MISSING_USER_MESSAGE));
        return instanceService.findAllByOwner(user, domain, pageable).getContent().stream()
                .map(this::mapAppInstanceBase)
                .collect(Collectors.toList());
    }

    @GetMapping("/{appInstanceId}")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'READ')")
    @Transactional
    public AppInstanceViewExtended getAppInstance(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                                  @NotNull Principal principal) {
        AppInstance appInstance = instanceService.find(appInstanceId)
                .orElseThrow(() -> new MissingElementException("App instance not found."));
        return mapAppInstanceExtended(appInstance);
    }

    @PostMapping("/domain/{domainId}")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
    @Transactional
    public Id createAppInstance(@RequestBody AppInstanceRequest appInstanceRequest,
                                @NotNull Principal principal,
                                @PathVariable Long domainId) {
        Application app = getApp(appInstanceRequest.getApplicationId());
        Domain domain = domainService.findDomain(domainId)
                .orElseThrow(() -> new MissingElementException("Domain not found"));
        AppInstance appInstance;
        /*
        check name uniqueness
        forbidden names - names of all app instances in domain, where state is different from 'DONE' and 'REMOVED'
         */
        Set<String> forbiddenNames = instanceService.findAllByDomain(domain).stream() // get all app instances in domain
                .filter(appInst -> {
                    AppInstanceState state = mapAppInstanceState(appDeploymentMonitor.state(appInst.getInternalId())); // map their internal state to app instance state
                    return !(state.equals(AppInstanceState.DONE) || state.equals(AppInstanceState.REMOVED)); // check if it does not equal 'DONE' or 'REMOVED'
                })
                .map(AppInstance::getName) // take names only
                .collect(Collectors.toSet());
        if (forbiddenNames.contains(appInstanceRequest.getName())) {
            throw new IllegalArgumentException("Name is already taken");
        }

        try {
            appInstance = instanceService.create(domain, app, appInstanceRequest.getName(), appInstanceRequest.isAutoUpgradesEnabled());
        } catch (ApplicationSubscriptionNotActiveException e) {
            throw new ProcessingException("Unable to create instance. " + e.getMessage());
        }

        AppDeployment appDeployment = AppDeployment.builder()
                .domain(domain.getCodename())
                .instanceId(appInstance.getId())
                .applicationId(Identifier.newInstance(appInstance.getApplication().getId()))
                .deploymentName(appInstance.getName())
                .configFileRepositoryRequired(app.getAppConfigurationSpec().isConfigFileRepositoryRequired())
                .configUpdateEnabled(app.getAppConfigurationSpec().isConfigUpdateEnabled())
                /*
                 * NMAAS-967
                 * information if terms acceptance are required is passed to app deployment
                 */
                .termsAcceptanceRequired(app.getAppConfigurationSpec().isTermsAcceptanceRequired())
                .owner(principal.getName())
                .appName(app.getName())
                .descriptiveDeploymentId(createDescriptiveDeploymentId(domain.getCodename(), app.getName(), appInstance.getId()))
                .build();

        Identifier internalId = appLifecycleManager.deployApplication(appDeployment);
        appInstance.setInternalId(internalId);

        instanceService.update(appInstance);

        return new Id(appInstance.getId());
    }

    public static Identifier createDescriptiveDeploymentId(String domain, String appName, Long appInstanceNumber) {
        return Identifier.newInstance(
                String.join("-", domain, appName.replace(" ", ""), String.valueOf(appInstanceNumber)).toLowerCase()
        );
    }

    @DeleteMapping("/{appInstanceId}")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'DELETE')")
    @Transactional
    public void deleteAppInstance(@PathVariable(value = "appInstanceId") Long appInstanceId) {
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
    public void removeFailedInstance(@PathVariable(value = "appInstanceId") Long appInstanceId) {
        try {
            AppInstance appInstance = getAppInstance(appInstanceId);
            appLifecycleManager.removeFailedApplication(appInstance.getInternalId());
        } catch (InvalidDeploymentIdException e) {
            throw new ProcessingException(MISSING_APP_INSTANCE_MESSAGE);
        }
    }

    @GetMapping("/{appInstanceId}/state")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'READ')")
    @Transactional
    public AppInstanceStatus getState(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                      @NotNull Principal principal) {
        AppInstance appInstance = getAppInstance(appInstanceId);
        return getAppInstanceState(appInstance);
    }

    @GetMapping("/{appInstanceId}/state/history")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'READ')")
    @Transactional
    public List<AppDeploymentHistoryView> getStateHistory(@PathVariable(value = "appInstanceId") Long appInstanceId) {
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

    @PostMapping("/{appInstanceId}/redeploy")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public void redeployAppInstance(@PathVariable(value = "appInstanceId") Long appInstanceId) {
        try {
            AppInstance appInstance = getAppInstance(appInstanceId);
            this.appLifecycleManager.redeployApplication(appInstance.getInternalId());
        } catch (InvalidDeploymentIdException e) {
            throw new ProcessingException(MISSING_APP_INSTANCE_MESSAGE);
        }
    }

    @PostMapping("/{appInstanceId}/upgrade/{targetApplicationId}")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public void upgradeAppInstance(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                   @PathVariable(value = "targetApplicationId") Long targetApplicationId) {
        try {
            AppInstance appInstance = getAppInstance(appInstanceId);
            this.appLifecycleManager.upgradeApplication(appInstance.getInternalId(), Identifier.newInstance(targetApplicationId));
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

        List<User> usersToAdd = toAddMemberUsernames.stream()
                .map(this::getUser)
                .filter(u -> !u.getSshKeys().isEmpty()) // skip users with no ssh keys
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getDomain().getId().equals(appInstance.getDomain().getId()))) // allow only users with role in app instance domain
                .collect(Collectors.toList()); // retrieve users from usernames to be added to members

        appInstance.getMembers().addAll(new HashSet<>(usersToAdd));
        this.instanceService.update(appInstance);

        // get user data to be removed from members
        List<User> usersToRemove = oldMembers.stream().filter(m -> toRemoveMemberUsernames.contains(m.getUsername())).collect(Collectors.toList());

        usersToRemove.forEach( r -> {
            RemoveUserFromRepositoryGitlabEvent event = new RemoveUserFromRepositoryGitlabEvent(
                    "AppInstance members list update",
                    r.getUsername(),
                    appInstance.getInternalId()
            );
            eventPublisher.publishEvent(event);
        });

        usersToAdd.forEach( a -> {
            if(a.getSshKeys().isEmpty()) {
                log.info(String.format("[ADD USER TO GITLAB REPO] User [%s] does not have any ssh keys, skipping", a.getUsername()));
            } else {
                AddUserToRepositoryGitlabEvent event = new AddUserToRepositoryGitlabEvent(
                        "AppInstance members list update",
                        a.getUsername(),
                        a.getEmail(),
                        a.getFirstname() + " " + a.getLastname(),
                        a.getSshKeys().stream().map(SSHKeyEntity::getKeyValue).collect(Collectors.toList()),
                        appInstance.getInternalId()
                );
                eventPublisher.publishEvent(event);
            }
        });
    }

    /**
     * provides deployment statistics/current number of applications of each type
     * @return result map
     */
    @GetMapping("/statistics")
    public Map<String, Long> deploymentStatistics() {
        return this.appDeploymentRepositoryManager.getDeploymentStatistics();
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
            case APPLICATION_UPGRADE_IN_PROGRESS:
            case APPLICATION_UPGRADED:
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

        // explicitly set application base
        ApplicationBase applicationBase = this.appBaseService.findByName(appInstance.getApplication().getName());
        ai.getApplication().setApplicationBase(modelMapper.map(applicationBase, ApplicationBaseView.class));

        return (AppInstanceViewExtended) addAppInstanceProperties(ai, appInstance);
    }

    private AppInstanceBase mapAppInstanceBase(AppInstance appInstance) {
        if (appInstance == null) {
            return null;
        }
        AppInstanceBase ai = modelMapper.map(appInstance, AppInstanceBase.class);
        return addAppInstanceBaseProperties(ai, appInstance);
    }

    private AppInstanceBase addAppInstanceBaseProperties(AppInstanceBase ai, AppInstance appInstance) {
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

        // add information about app instance upgrade possibility
        if (!List.of(AppInstanceState.DONE, AppInstanceState.REMOVED).contains(ai.getState())) {
            ai.setUpgradePossible(instanceService.checkUpgradePossible(appInstance.getId()));
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
            ai.setConfigWizardTemplate(new ConfigWizardTemplateView(
                    appInstance.getApplication().getConfigWizardTemplate().getId(),
                    appInstance.getApplication().getConfigWizardTemplate().getTemplate()));
        } catch (Exception e) {
            ai.setConfigWizardTemplate(null);
        }

        try {
            ai.setConfigUpdateWizardTemplate(new ConfigWizardTemplateView(
                    appInstance.getApplication().getConfigUpdateWizardTemplate().getId(),
                    appInstance.getApplication().getConfigUpdateWizardTemplate().getTemplate()));
        } catch (Exception e) {
            ai.setConfigUpdateWizardTemplate(null);
        }

        // add app instance upgrade details (might be null as well)
        ai.setUpgradeInfo(instanceService.obtainUpgradeInfo(appInstance.getId()));

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

        if (user.getRoles().stream().anyMatch((UserRole ur) -> ur.getRole().equals(Role.ROLE_SYSTEM_ADMIN))) {
            isSystemAdmin = true;
        }

        if (domainId.equals(domainService.getGlobalDomain().orElseThrow(() -> new InvalidDomainException("Global Domain not found")).getId())) {
            isDomainGlobal = true;
        }

        return isSystemAdmin && isDomainGlobal;
    }

    private Pageable pageableValidator(Pageable pageable) {
        if (!this.isPageableValidForAppInstance(pageable)) {
            return null;
        }
        return pageable;
    }

}
