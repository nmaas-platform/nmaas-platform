package net.geant.nmaas.portal.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.api.model.AppConfigurationView;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.events.app.AppAutoDeploymentStatusUpdateEvent;
import net.geant.nmaas.orchestration.events.app.AppAutoDeploymentTriggeredEvent;
import net.geant.nmaas.portal.api.bulk.BulkDeploymentViewS;
import net.geant.nmaas.portal.api.bulk.BulkType;
import net.geant.nmaas.portal.api.bulk.CsvApplication;
import net.geant.nmaas.portal.api.domain.AppInstanceState;
import net.geant.nmaas.portal.api.domain.UserViewMinimal;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.BulkDeployment;
import net.geant.nmaas.portal.persistent.entity.BulkDeploymentEntry;
import net.geant.nmaas.portal.persistent.entity.BulkDeploymentState;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.repositories.BulkDeploymentEntryRepository;
import net.geant.nmaas.portal.persistent.repositories.BulkDeploymentRepository;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.ApplicationSubscriptionService;
import net.geant.nmaas.portal.service.BulkApplicationService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView.BULK_ENTRY_DETAIL_KEY_APP_INSTANCE_ID;
import static net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView.BULK_ENTRY_DETAIL_KEY_APP_INSTANCE_NAME;
import static net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView.BULK_ENTRY_DETAIL_KEY_DOMAIN_CODENAME;
import static net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView.BULK_ENTRY_DETAIL_KEY_DOMAIN_NAME;
import static net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView.BULK_ENTRY_DETAIL_KEY_ERROR_MESSAGE;
import static net.geant.nmaas.portal.api.market.AppInstanceController.createDescriptiveDeploymentId;
import static net.geant.nmaas.portal.api.market.AppInstanceController.mapAppInstanceState;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkApplicationServiceImpl implements BulkApplicationService {

    private final static int WAIT_INTERVAL_IN_SECONDS = 15;

    private final ApplicationBaseService applicationBaseService;
    private final ApplicationService applicationService;
    private final DomainService domainService;
    private final ApplicationSubscriptionService applicationSubscriptionService;

    private final ApplicationInstanceService instanceService;
    private final AppDeploymentMonitor appDeploymentMonitor;
    private final AppLifecycleManager appLifecycleManager;

    private final BulkDeploymentRepository bulkDeploymentRepository;
    private final BulkDeploymentEntryRepository bulkDeploymentEntryRepository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public BulkDeploymentViewS handleBulkDeployment(String applicationName, List<CsvApplication> appInstanceSpecs, UserViewMinimal creator) {
        log.info("Handling bulk application deployment for {} with {} entries", applicationName, appInstanceSpecs.size());

        if (!applicationBaseService.exists(applicationName)) {
            throw new IllegalArgumentException("Application with given name doesn't exist");
        }

        // create base bulk deployment record
        BulkDeployment bulkDeployment = createBulkDeployment(creator);

        appInstanceSpecs.forEach(applicationSpec -> {
            AppInstance instance = null;
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
                instance = instanceService.create(domain, application, applicationSpec.getApplicationInstanceName(), false);

                // preparing and triggering new application deployment
                AppDeployment appDeployment = AppDeployment.builder()
                        .domain(instance.getDomain().getCodename())
                        .instanceId(instance.getId())
                        .applicationId(Identifier.newInstance(instance.getApplication().getId()))
                        .deploymentName(instance.getName())
                        .configFileRepositoryRequired(application.getAppConfigurationSpec().isConfigFileRepositoryRequired())
                        .configUpdateEnabled(application.getAppConfigurationSpec().isConfigUpdateEnabled())
                        .termsAcceptanceRequired(application.getAppConfigurationSpec().isTermsAcceptanceRequired())
                        .owner(creator.getUsername())
                        .appName(application.getName())
                        .descriptiveDeploymentId(createDescriptiveDeploymentId(instance.getDomain().getCodename(), application.getName(), instance.getId()))
                        .build();
                Identifier internalId = appLifecycleManager.deployApplication(appDeployment);

                // updating application instance information with assigned deployment identifier
                instance.setInternalId(internalId);
                instanceService.update(instance);

                // updating application instance information with custom configuration
                AppConfigurationView appConfigurationView = new AppConfigurationView();
                if (Objects.nonNull(applicationSpec.getParameters())) {
                    String configJson = new ObjectMapper().writeValueAsString(applicationSpec.getParameters());
                    instance.setConfiguration(configJson);
                    appConfigurationView.setMandatoryParameters(configJson);
                    instanceService.update(instance);
                }

                // store entry information in database
                BulkDeploymentEntry bulkDeploymentEntry = bulkDeploymentEntryRepository.save(
                        BulkDeploymentEntry.builder()
                                .type(BulkType.APPLICATION)
                                .state(BulkDeploymentState.PROCESSING)
                                .created(true)
                                .details(prepareBulkApplicationDeploymentDetailsMap(instance))
                                .build()
                );

                bulkDeployment.getEntries().add(bulkDeploymentEntry);

                // triggering event for monitoring and processing of bulk deployment
                eventPublisher.publishEvent(
                        new AppAutoDeploymentTriggeredEvent(
                                this,
                                Identifier.newInstance(bulkDeploymentEntry.getId()),
                                internalId,
                                appConfigurationView));

            } catch (Exception e) {
                log.warn("Exception thrown while deploying application {}:{} in domain {}", applicationName, applicationSpec.getApplicationVersion(), applicationSpec.getDomainName());
                log.warn(e.getMessage());
                bulkDeployment.getEntries().add(
                        BulkDeploymentEntry.builder()
                                .type(BulkType.APPLICATION)
                                .state(BulkDeploymentState.FAILED)
                                .created(false)
                                .details(prepareBulkApplicationDeploymentDetailsMap(instance, e.getMessage()))
                                .build());
            }
        });

        return modelMapper.map(bulkDeploymentRepository.save(bulkDeployment), BulkDeploymentViewS.class);
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

    @Override
    @EventListener
    @Transactional
    @Loggable(LogLevel.INFO)
    public ApplicationEvent handleDeploymentStatusUpdate(AppAutoDeploymentStatusUpdateEvent event) {
        log.info("Status update for deployment {}", event.getDeploymentId());
        BulkDeploymentEntry bulkDeploymentEntry = bulkDeploymentEntryRepository.findById(event.getBulkDeploymentId().longValue()).orElseThrow();
        try {
            AppLifecycleState appLifecycleState = appDeploymentMonitor.state(event.getDeploymentId());
            switch (appLifecycleState) {
                case APPLICATION_DEPLOYMENT_VERIFIED:
                    bulkDeploymentEntry.setState(BulkDeploymentState.COMPLETED);
                    bulkDeploymentEntryRepository.save(bulkDeploymentEntry);
                    // TODO update state of the entire bulk object
                    return null;
                case APPLICATION_CONFIGURATION_FAILED:
                case APPLICATION_DEPLOYMENT_FAILED:
                case APPLICATION_DEPLOYMENT_VERIFICATION_FAILED:
                    bulkDeploymentEntry.setState(BulkDeploymentState.FAILED);
                    bulkDeploymentEntryRepository.save(bulkDeploymentEntry);
                    // TODO update state of the entire bulk object
                    return null;
                default:
                    Thread.sleep(event.getWaitIntervalBeforeNextCheckInMillis() > 0 ?
                            event.getWaitIntervalBeforeNextCheckInMillis() : WAIT_INTERVAL_IN_SECONDS * 1000);
                    return event;
            }
        } catch (InterruptedException e) {
            log.warn("Thread interrupted while sleeping ... Resending the event");
            return event;
        } catch (NoSuchElementException e) {
            log.warn("Received bulk status update request but entry was not found ({})", event.getBulkDeploymentId().toString());
            return null;
        }
    }

    private static BulkDeployment createBulkDeployment(UserViewMinimal creator) {
        BulkDeployment bulkDeployment = new BulkDeployment();
        bulkDeployment.setType(BulkType.APPLICATION);
        bulkDeployment.setState(BulkDeploymentState.PROCESSING);
        bulkDeployment.setCreatorId(creator.getId());
        bulkDeployment.setCreationDate(OffsetDateTime.now());
        bulkDeployment.setEntries(new ArrayList<>());
        return bulkDeployment;
    }

    private static Map<String, String> prepareBulkApplicationDeploymentDetailsMap(AppInstance appInstance, String errorMessage) {
        Map<String, String> details = prepareBulkApplicationDeploymentDetailsMap(appInstance);
        details.put(BULK_ENTRY_DETAIL_KEY_ERROR_MESSAGE, errorMessage);
        return details;
    }

    private static Map<String, String> prepareBulkApplicationDeploymentDetailsMap(AppInstance appInstance) {
        Map<String, String> details = new HashMap<>();
        if (Objects.nonNull(appInstance)) {
            details.put(BULK_ENTRY_DETAIL_KEY_APP_INSTANCE_ID, appInstance.getId().toString());
            details.put(BULK_ENTRY_DETAIL_KEY_APP_INSTANCE_NAME, appInstance.getName());
            details.put(BULK_ENTRY_DETAIL_KEY_DOMAIN_NAME, appInstance.getDomain().getName());
            details.put(BULK_ENTRY_DETAIL_KEY_DOMAIN_CODENAME, appInstance.getDomain().getCodename());
        }
        return details;
    }

}
