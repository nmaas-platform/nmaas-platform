package net.geant.nmaas.portal.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.events.app.AppAutoDeploymentTriggeredEvent;
import net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView;
import net.geant.nmaas.portal.api.bulk.BulkType;
import net.geant.nmaas.portal.api.bulk.CsvApplication;
import net.geant.nmaas.portal.api.domain.AppInstanceState;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.ApplicationSubscriptionService;
import net.geant.nmaas.portal.service.BulkApplicationService;
import net.geant.nmaas.portal.service.DomainService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static net.geant.nmaas.portal.api.market.AppInstanceController.createDescriptiveDeploymentId;
import static net.geant.nmaas.portal.api.market.AppInstanceController.mapAppInstanceState;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkApplicationServiceImpl implements BulkApplicationService {

    private final ApplicationBaseService applicationBaseService;
    private final ApplicationService applicationService;
    private final DomainService domainService;
    private final ApplicationSubscriptionService applicationSubscriptionService;

    private final ApplicationInstanceService instanceService;
    private final AppDeploymentMonitor appDeploymentMonitor;
    private final AppLifecycleManager appLifecycleManager;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public List<BulkDeploymentEntryView> handleBulkCreation(String owner, String applicationName, List<CsvApplication> appInstanceSpecs) {
        log.info("Handling bulk application deployment for {} with {} entries", applicationName, appInstanceSpecs.size());

        List<BulkDeploymentEntryView> result = new ArrayList<>();

        if (!applicationBaseService.exists(applicationName)) {
            throw new IllegalArgumentException("Application with given name doesn't exist");
        }

        appInstanceSpecs.forEach(applicationSpec -> {
            try {
                // loading requested application version
                Application application = findApplication(applicationName, applicationSpec.getApplicationVersion());

                // loading target domain
                Domain domain = domainService.findDomain(applicationSpec.getDomainName())
                        .orElseThrow(() -> new MissingElementException("Domain not found"));

                // making new application subscription in given domain
                applicationSubscriptionService.subscribe(application.getId(), domain.getId(), true);

                // verifying if desired instance name is still available
                verifyIfInstanceNameIsAvailable(applicationSpec, domain);

                // creating initial application instance entry
                AppInstance instance = instanceService.create(domain, application, applicationSpec.getApplicationInstanceName(), false);

                // preparing and triggering new application deployment
                AppDeployment appDeployment = AppDeployment.builder()
                        .domain(instance.getDomain().getCodename())
                        .instanceId(instance.getId())
                        .applicationId(Identifier.newInstance(instance.getApplication().getId()))
                        .deploymentName(instance.getName())
                        .configFileRepositoryRequired(application.getAppConfigurationSpec().isConfigFileRepositoryRequired())
                        .configUpdateEnabled(application.getAppConfigurationSpec().isConfigUpdateEnabled())
                        .termsAcceptanceRequired(application.getAppConfigurationSpec().isTermsAcceptanceRequired())
                        .owner(owner)
                        .appName(application.getName())
                        .descriptiveDeploymentId(createDescriptiveDeploymentId(instance.getDomain().getCodename(), application.getName(), instance.getId()))
                        .build();
                Identifier internalId = appLifecycleManager.deployApplication(appDeployment);

                // updating application instance information
                instance.setInternalId(internalId);
                instance.setConfiguration(new ObjectMapper().writeValueAsString(applicationSpec.getParameters()));
                instanceService.update(instance);

                // triggering event for monitoring and processing of bulk deployment
                eventPublisher.publishEvent(new AppAutoDeploymentTriggeredEvent(this, internalId, applicationSpec.getParameters()));

            } catch (Exception e) {
                log.warn("Exception thrown while deploying application {}:{} in domain {}", applicationName, applicationSpec.getApplicationVersion(), applicationSpec.getDomainName());
                log.warn(e.getMessage());
                result.add(BulkDeploymentEntryView.builder().type(BulkType.APPLICATION).successful(false).created(false).details(null).build());
            }
            result.add(BulkDeploymentEntryView.builder().type(BulkType.APPLICATION).successful(true).created(true).details(null).build());
        });

        return result;
    }

    private Application findApplication(String appName, String version) {
        return applicationService.findApplication(appName, version)
                .orElseThrow(() -> new MissingElementException("Application name=" + appName + " not found."));
    }

    private void verifyIfInstanceNameIsAvailable(CsvApplication applicationSpec, Domain domain) {
        // get all app instances in domain
        Set<String> forbiddenNames = instanceService.findAllByDomain(domain).stream()
                .filter(appInst -> {
                    // map their internal state to app instance state
                    AppInstanceState state = mapAppInstanceState(appDeploymentMonitor.state(appInst.getInternalId()));
                    // check if it does not equal 'DONE' or 'REMOVED'
                    return !(state.equals(AppInstanceState.DONE) || state.equals(AppInstanceState.REMOVED));
                })
                .map(AppInstance::getName)
                .collect(Collectors.toSet());
        if (forbiddenNames.contains(applicationSpec.getApplicationInstanceName())) {
            throw new IllegalArgumentException("Name is already taken");
        }
    }

}
