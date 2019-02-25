package net.geant.nmaas.portal.api.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.api.model.AppDeploymentHistoryView;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppLifecycleState;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.api.domain.AppDeploymentSpec;
import net.geant.nmaas.portal.api.domain.AppInstanceState;
import net.geant.nmaas.portal.api.domain.AppInstanceStatus;
import net.geant.nmaas.portal.api.domain.AppInstanceSubscription;
import net.geant.nmaas.portal.api.domain.AppInstanceView;
import net.geant.nmaas.portal.api.domain.ConfigTemplate;
import net.geant.nmaas.portal.api.domain.Id;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.exceptions.ApplicationSubscriptionNotActiveException;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.DomainService;
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
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class AppInstanceController extends AppBaseController {

    private static final String MISSING_APP_INSTANCE_MESSAGE = "Missing app instance";

    private AppLifecycleManager appLifecycleManager;

    private AppDeploymentMonitor appDeploymentMonitor;

    private ApplicationInstanceService instances;

    private DomainService domains;

    private ObjectMapper objectMapper;

    @GetMapping("/apps/instances")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @Transactional
    public List<AppInstanceView> getAllInstances(Pageable pageable) {
        return instances.findAll(pageable).getContent().stream()
                .map(this::mapAppInstance)
                .collect(Collectors.toList());
    }

    @GetMapping("/apps/instances/my")
    @Transactional
    public List<AppInstanceView> getMyAllInstances(@NotNull Principal principal, Pageable pageable) {
        User user = users.findByUsername(principal.getName()).orElseThrow(() -> new MissingElementException("User not found"));
        return instances.findAllByOwner(user, pageable).getContent().stream()
                .map(this::mapAppInstance)
                .collect(Collectors.toList());
    }

    @GetMapping("/domains/{domainId}/apps/instances")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'ANY')")
    @Transactional
    public List<AppInstanceView> getAllInstances(@PathVariable Long domainId, Pageable pageable) {
        Domain domain = domains.findDomain(domainId).orElseThrow(() -> new MissingElementException("Domain " + domainId + " not found"));
        return instances.findAllByDomain(domain, pageable).getContent().stream()
                .map(this::mapAppInstance)
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/domains/{domainId}/apps/instances/my")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'ANY')")
    @Transactional
    public List<AppInstanceView> getMyAllInstances(@PathVariable Long domainId, @NotNull Principal principal, Pageable pageable) {
        return getUserDomainAppInstances(domainId, principal.getName(), pageable);
    }

    @GetMapping("/domains/{domainId}/apps/instances/user/{username}")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
    @Transactional
    public List<AppInstanceView> getUserAllInstances(@PathVariable Long domainId, @PathVariable String username, Pageable pageable){
        return getUserDomainAppInstances(domainId, username, pageable);
    }

    private List<AppInstanceView> getUserDomainAppInstances(Long domainId, String username, Pageable pageable) {
        Domain domain = domains.findDomain(domainId)
                .orElseThrow(() -> new MissingElementException("Domain " + domainId + " not found"));
        User user = users.findByUsername(username)
                .orElseThrow(() -> new MissingElementException("User not found"));
        return instances.findAllByOwner(user, domain, pageable).getContent().stream()
                .map(this::mapAppInstance)
                .collect(Collectors.toList());
    }

    @GetMapping({"/apps/instances/{appInstanceId}", "/domains/{domainId}/apps/instances/{appInstanceId}"})
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public AppInstanceView getAppInstance(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                          @NotNull Principal principal) {
        AppInstance appInstance = instances.find(appInstanceId)
                .orElseThrow(() -> new MissingElementException("App instance not found."));
        return mapAppInstance(appInstance);
    }

    @PostMapping("/domains/{domainId}/apps/instances")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'CREATE')")
    @Transactional
    public Id createAppInstance(@RequestBody(required = true) AppInstanceSubscription appInstanceSubscription,
                                @NotNull Principal principal, @PathVariable Long domainId) {
        Application app = getApp(appInstanceSubscription.getApplicationId());
        Domain domain = domains.findDomain(domainId)
                .orElseThrow(() -> new MissingElementException("Domain not found"));
        AppInstance appInstance;
        try {
            appInstance = instances.create(domain, app, appInstanceSubscription.getName());
        } catch (ApplicationSubscriptionNotActiveException e) {
            throw new ProcessingException("Unable to create instance. " + e.getMessage());
        }

        AppDeploymentSpec appDeploymentSpec = modelMapper.map(app.getAppDeploymentSpec(), AppDeploymentSpec.class);
        AppDeployment appDeployment = AppDeployment.builder()
                .domain(domain.getCodename())
                .deploymentId(Identifier.newInstance(appInstance.getApplication().getId()))
                .applicationId(Identifier.newInstance(appInstance.getApplication().getId()))
                .deploymentName(appInstance.getName())
                .configFileRepositoryRequired(appDeploymentSpec.isConfigFileRepositoryRequired())
                .storageSpace(appDeploymentSpec.getDefaultStorageSpace())
                .owner(principal.getName())
                .appName(app.getName())
                .build();

        Identifier internalId = appLifecycleManager.deployApplication(appDeployment);
        appInstance.setInternalId(internalId);

        instances.update(appInstance);

        return new Id(appInstance.getId());
    }

    @PostMapping({"/apps/instances/{appInstanceId}/redeploy", "/domains/{domainId}/apps/instances/{appInstanceId}/redeploy"})
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

    @DeleteMapping({"/apps/instances/{appInstanceId}", "/domains/{domainId}/apps/instances/{appInstanceId}"})
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

    @GetMapping({"/apps/instances/{appInstanceId}/state", "/domains/{domainId}/apps/instances/{appInstanceId}/state"})
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public AppInstanceStatus getState(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                      @NotNull Principal principal) {
        AppInstance appInstance = getAppInstance(appInstanceId);
        return getAppInstanceState(appInstance);
    }

    @GetMapping({"/apps/instances/{appInstanceId}/state/history", "/domains/{domainId}/apps/instances/{appInstanceId}/state/history"})
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

    //domainId is not used in this method.
    @PostMapping({"/apps/instances/{appInstanceId}/restart", "/domains/{domainId}/apps/instances/{appInstanceId}/restart"})
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
                appInstanceState = AppInstanceState.FAILURE;
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

        if(appInstance.getApplication().getName().equalsIgnoreCase("Grafana")){
            try{
                ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(appInstance.getApplication().getConfigTemplate().getTemplate());
                ObjectNode updatedNode = objectMapper.createObjectNode().put("values", objectMapper.writeValueAsString(this.getAllInstanceNamesByApplicationNameOwnerAndDomain("Prometheus", appInstance.getOwner(), appInstance.getDomain())));
                jsonNode.findParent("data").replace("data", updatedNode);
                ai.setConfigTemplate(new ConfigTemplate(objectMapper.writeValueAsString(jsonNode)));
            } catch(IOException e){
                throw new IllegalArgumentException(e.getMessage());
            }
        } else{
            ai.setConfigTemplate(new ConfigTemplate(appInstance.getApplication().getConfigTemplate().getTemplate()));
        }

        return ai;
    }

    private Map<Long, String> getAllInstanceNamesByApplicationNameOwnerAndDomain(String appName, User owner, Domain domain){
        return this.instances.getAllInstanceNamesByApplicationNameOwnerAndDomain(appName, owner, domain).stream()
                .filter(app -> appDeploymentMonitor.state(app.getInternalId()).equals(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED))
                .collect(Collectors.toMap(AppInstance::getId, AppInstance::getName));
    }
}
