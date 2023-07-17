package net.geant.nmaas.portal.service.impl;


import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView;
import net.geant.nmaas.portal.api.bulk.CsvApplication;
import net.geant.nmaas.portal.api.bulk.CsvBean;
import net.geant.nmaas.portal.api.bulk.CsvDomain;
import net.geant.nmaas.portal.api.domain.AppInstanceState;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.exceptions.ApplicationSubscriptionNotActiveException;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.ApplicationSubscriptionService;
import net.geant.nmaas.portal.service.BulkApplicationService;
import net.geant.nmaas.portal.service.BulkDomainService;
import net.geant.nmaas.portal.service.DomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BulkApplicationServiceImpl implements BulkApplicationService {

    private final ApplicationService applicationService;

    private final DomainService domainService;

    private final ApplicationInstanceService instanceService;

    private final AppDeploymentMonitor appDeploymentMonitor;

    private final AppLifecycleManager appLifecycleManager;

    private final ApplicationSubscriptionService appSubscriptions;


    public BulkApplicationServiceImpl(ApplicationService applicationService,
                                  DomainService domainService,
                                  ApplicationInstanceService instanceService,
                                  AppDeploymentMonitor appDeploymentMonitor,
                                  AppLifecycleManager appLifecycleManager,
                                      ApplicationSubscriptionService appSubscriptions) {
        this.applicationService = applicationService;
        this.domainService = domainService;
        this.instanceService = instanceService;
        this.appDeploymentMonitor = appDeploymentMonitor;
        this.appLifecycleManager = appLifecycleManager;
        this.appSubscriptions = appSubscriptions;
    }


    @Override
    public List<BulkDeploymentEntryView> handleBulkCreation(List<CsvBean> apps) {
        log.info("Handling bulk application creation with {} entries", apps.size());

        List<BulkDeploymentEntryView> result = new ArrayList<>();
        List<CsvApplication> csvApps= apps.stream().map(d -> (CsvApplication) d).collect(Collectors.toList());

        if(!csvApps.stream().allMatch(csvApplication -> csvApplication.getApplicationName().equals(csvApps.get(0).getApplicationName()))) {
            throw new ProcessingException("App name is not the same for all instances");
        };

        Application app = findApplication(csvApps.get(0).getApplicationName(), csvApps.get(0).getApplicationVersion());
        List<Domain> domains = new ArrayList<>();
        csvApps.forEach(ap -> {
            Domain domain = domainService.findDomain(ap.getDomainName())
                    .orElseThrow(() -> new MissingElementException("Domain not found"));
            domains.add(domain);
            this.appSubscriptions.subscribe(app.getId(), domain.getId(), true);
        });

        domains.forEach(domain -> {
            Set<String> forbiddenNames = instanceService.findAllByDomain(domain).stream() // get all app instances in domain
                    .filter(appInst -> {
                        AppInstanceState state = mapAppInstanceState(appDeploymentMonitor.state(appInst.getInternalId())); // map their internal state to app instance state
                        return !(state.equals(AppInstanceState.DONE) || state.equals(AppInstanceState.REMOVED)); // check if it does not equal 'DONE' or 'REMOVED'
                    })
                    .map(AppInstance::getName) // take names only
                    .collect(Collectors.toSet());

            csvApps.forEach(ap -> {
                if(forbiddenNames.contains(ap.getApplicationInstanceName())) {
                    throw new IllegalArgumentException("Name is already taken");
                }
            });
        });

        List<AppInstance> instances = new ArrayList<>();

        try {
            csvApps.forEach(ap -> {
                AppInstance instance = instanceService.create(getDomainForApp(domains,ap), app, ap.getApplicationInstanceName(), false);
                instances.add(instance);
            });
        } catch (ApplicationSubscriptionNotActiveException e) {
            throw new ProcessingException("Unable to create instance. " + e.getMessage());
        }

        instances.forEach(ap -> {
            AppDeployment appDeployment = AppDeployment.builder()
                    .domain(ap.getDomain().getCodename())
                    .instanceId(ap.getId())
                    .applicationId(Identifier.newInstance(ap.getApplication().getId()))
                    .deploymentName(ap.getName())
                    .configFileRepositoryRequired(app.getAppConfigurationSpec().isConfigFileRepositoryRequired())
                    .configUpdateEnabled(app.getAppConfigurationSpec().isConfigUpdateEnabled())
                    /*
                     * NMAAS-967
                     * information if terms acceptance are required is passed to app deployment
                     */
                    .termsAcceptanceRequired(app.getAppConfigurationSpec().isTermsAcceptanceRequired())
//                    .owner(principal.getName())
                    .appName(app.getName())
                    .descriptiveDeploymentId(createDescriptiveDeploymentId(ap.getDomain().getCodename(), app.getName(), ap.getId()))
                    .build();

            Identifier internalId = appLifecycleManager.deployApplication(appDeployment);
            ap.setInternalId(internalId);



            instanceService.update(ap);
        });

        return null;
    }

    private Application findApplication(String appName, String version) {
        return applicationService.findApplication(appName, version).orElseThrow(() -> new MissingElementException("Application name=" + appName + " not found."));
    }

    private Identifier createDescriptiveDeploymentId(String domain, String appName, Long appInstanceNumber) {
        return Identifier.newInstance(
                String.join("-", domain, appName.replace(" ", ""), String.valueOf(appInstanceNumber)).toLowerCase()
        );
    }

    private Domain getDomainForApp(List<Domain> domains, CsvApplication app) {
        return domains.stream().filter(domain -> domain.getName().equals(app.getDomainName())).limit(1).collect(Collectors.toList()).get(0);
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
}
