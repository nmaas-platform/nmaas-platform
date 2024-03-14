package net.geant.nmaas.portal.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.api.model.AppConfigurationView;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.events.app.AppAutoDeploymentReviewEvent;
import net.geant.nmaas.orchestration.events.app.AppAutoDeploymentStatusUpdateEvent;
import net.geant.nmaas.orchestration.events.app.AppAutoDeploymentTriggeredEvent;
import net.geant.nmaas.portal.api.bulk.BulkAppDetails;
import net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView;
import net.geant.nmaas.portal.api.bulk.BulkDeploymentView;
import net.geant.nmaas.portal.api.bulk.BulkDeploymentViewS;
import net.geant.nmaas.portal.api.bulk.BulkType;
import net.geant.nmaas.portal.api.bulk.CsvApplication;
import net.geant.nmaas.portal.api.domain.AppInstanceState;
import net.geant.nmaas.portal.api.domain.UserViewMinimal;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
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
import org.apache.commons.collections4.MultiValuedMap;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView.BULK_ENTRY_DETAIL_KEY_APP_ID;
import static net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView.BULK_ENTRY_DETAIL_KEY_APP_INSTANCE_ID;
import static net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView.BULK_ENTRY_DETAIL_KEY_APP_INSTANCE_NAME;
import static net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView.BULK_ENTRY_DETAIL_KEY_APP_NAME;
import static net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView.BULK_ENTRY_DETAIL_KEY_DOMAIN_CODENAME;
import static net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView.BULK_ENTRY_DETAIL_KEY_DOMAIN_NAME;
import static net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView.BULK_ENTRY_DETAIL_KEY_ERROR_MESSAGE;
import static net.geant.nmaas.portal.api.market.AppInstanceController.createDescriptiveDeploymentId;
import static net.geant.nmaas.portal.api.market.AppInstanceController.mapAppInstanceState;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkApplicationServiceImpl implements BulkApplicationService {

    private static final int DEFAULT_DELAY_IN_SECONDS = 15;
    private static final String CSV_HEADER_PARAM_PREFIX = "param.";
    private static final String EMPTY_VALUE = "<EMPTY>";

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
        Long applicationBaseId = findApplicationBaseId(applicationName);

        // create base bulk deployment record
        BulkDeployment bulkDeployment = createBulkDeployment(creator);

        appInstanceSpecs.forEach(applicationSpec -> {
            AppInstance instance = null;
            Application application = null;
            try {
                // loading requested application version
                application = findApplication(applicationName, applicationSpec.getApplicationVersion());

                // loading target domain
                Domain domain = domainService.findDomain(applicationSpec.getDomainName())
                        .orElseThrow(() -> new MissingElementException("Domain not found"));

                // making new application subscription in given domain (if required)
                if (!applicationSubscriptionService.existsSubscription(applicationBaseId, domain.getId())) {
                    applicationSubscriptionService.subscribe(applicationBaseId, domain.getId(), true);
                }

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
                    String configJson = new ObjectMapper().writeValueAsString(
                            mapToDeploymentParameters(applicationSpec.getParameters())
                    );
                    instance.setConfiguration(configJson);
                    appConfigurationView.setJsonInput(configJson);
                    appConfigurationView.setMandatoryParameters(configJson);
                    instanceService.update(instance);
                } else {
                    appConfigurationView.setJsonInput("{}");
                }

                // store entry information in database
                BulkDeploymentEntry bulkDeploymentEntry = bulkDeploymentEntryRepository.save(
                        BulkDeploymentEntry.builder()
                                .type(BulkType.APPLICATION)
                                .state(BulkDeploymentState.PROCESSING)
                                .created(true)
                                .details(prepareBulkApplicationDeploymentDetailsMap(instance, application))
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
                                .details(prepareBulkApplicationDeploymentDetailsMap(instance, applicationSpec, e.getMessage(), application))
                                .build());
            }
        });

        return modelMapper.map(bulkDeploymentRepository.save(bulkDeployment), BulkDeploymentViewS.class);
    }

    private static Map<String, String> mapToDeploymentParameters(MultiValuedMap<String, String> parsedParameters) {
        Map<String, String> deploymentParameters = new HashMap<>();
        parsedParameters.keySet().forEach(parsedKey ->
                deploymentParameters.put(
                        parsedKey.replaceFirst(CsvApplication.PARAM_COLUMN_PREFIX, ""),
                        parsedParameters.get(parsedKey).iterator().next()
                )
        );
        return deploymentParameters;
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
    public ApplicationEvent handleDeploymentStatusUpdate(AppAutoDeploymentStatusUpdateEvent event) {
        log.info("Status update for deployment {}", event.getDeploymentId());
        BulkDeploymentEntry bulkDeploymentEntry = bulkDeploymentEntryRepository.findById(event.getBulkDeploymentId().longValue()).orElseThrow();
        try {
            AppLifecycleState appLifecycleState = appDeploymentMonitor.state(event.getDeploymentId());
            switch (appLifecycleState) {
                case APPLICATION_DEPLOYMENT_VERIFIED:
                    bulkDeploymentEntry.setState(BulkDeploymentState.COMPLETED);
                    bulkDeploymentEntryRepository.save(bulkDeploymentEntry);
                    return new AppAutoDeploymentReviewEvent(this);
                case APPLICATION_CONFIGURATION_FAILED:
                case APPLICATION_DEPLOYMENT_FAILED:
                case APPLICATION_DEPLOYMENT_VERIFICATION_FAILED:
                    bulkDeploymentEntry.setState(BulkDeploymentState.FAILED);
                    bulkDeploymentEntryRepository.save(bulkDeploymentEntry);
                    return new AppAutoDeploymentReviewEvent(this);
                default:
                    int delayInSeconds = event.getWaitIntervalBeforeNextCheckInSeconds() > 0 ?
                            event.getWaitIntervalBeforeNextCheckInSeconds() : DEFAULT_DELAY_IN_SECONDS;
                    log.debug("Waiting for {} seconds before next check", delayInSeconds);
                    Thread.sleep(delayInSeconds * 1000L);
                    event.setEventTimeOutInSeconds(event.getEventTimeOutInSeconds() - delayInSeconds);
                    log.debug("Event {} timeout left {}", event.getBulkDeploymentId(), event.getEventTimeOutInSeconds());
                    if (event.getEventTimeOutInSeconds() > 0) {
                        return event;
                    }
                    log.warn("Bulk deployment timeout reached. Setting status to FAILED.");
                    bulkDeploymentEntry.setState(BulkDeploymentState.FAILED);
                    bulkDeploymentEntryRepository.save(bulkDeploymentEntry);
                    return new AppAutoDeploymentReviewEvent(this);
            }
        } catch (InterruptedException e) {
            log.warn("Thread interrupted while sleeping ... Resending the event");
            Thread.currentThread().interrupt();
            return event;
        } catch (NoSuchElementException e) {
            log.warn("Received bulk status update request but entry was not found ({})", event.getBulkDeploymentId().toString());
            return null;
        }
    }

    @Override
    @EventListener
    @Transactional
    public void handleDeploymentReview(AppAutoDeploymentReviewEvent event) {
        log.info("Reviewing all processed bulk deployments");
        List<BulkDeployment> deployments = bulkDeploymentRepository.findByTypeAndState(BulkType.APPLICATION, BulkDeploymentState.PROCESSING);
        log.debug("Loaded {} application bulk deployments", deployments.size());
        deployments.forEach(
                d -> {
                    boolean stateChanged = false;
                    if (d.getEntries().stream().allMatch(e -> BulkDeploymentState.COMPLETED.equals(e.getState()))) {
                        d.setState(BulkDeploymentState.COMPLETED);
                        stateChanged = true;
                    } else if (d.getEntries().stream().anyMatch(e -> BulkDeploymentState.FAILED.equals(e.getState()))) {
                        d.setState(BulkDeploymentState.PARTIALLY_FAILED);
                        stateChanged = true;
                    } else if (d.getEntries().stream().allMatch(e -> BulkDeploymentState.FAILED.equals(e.getState()))) {
                        d.setState(BulkDeploymentState.FAILED);
                        stateChanged = true;
                    }
                    if (stateChanged) {
                        logBulkStateUpdate(d.getId(), d.getState().name());
                        bulkDeploymentRepository.save(d);
                    } else {
                        log.debug("No status updates required");
                    }
                }
        );
    }

    @Override
    public void deleteAppInstancesFromBulk(BulkDeploymentView bulk) {
        List<BulkDeploymentEntryView> apps = bulk.getEntries();
        for (BulkDeploymentEntryView app : apps) {
            Long appInstanceId = Long.valueOf(findAppDetail(app, BULK_ENTRY_DETAIL_KEY_APP_INSTANCE_ID));
            AppInstance appInstance = instanceService.find(appInstanceId)
                    .orElseThrow(() -> new ObjectNotFoundException("App instance not found"));

            appLifecycleManager.removeApplication(appInstance.getInternalId());
            instanceService.delete(appInstanceId);
        }
    }

    private String findAppDetail(BulkDeploymentEntryView app, String key) {
        return Optional.ofNullable(app.getDetails().get(key))
                .orElseThrow(() -> new ObjectNotFoundException(key + " not found"));
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

    private Map<String, String> prepareBulkApplicationDeploymentDetailsMap(AppInstance appInstance, CsvApplication applicationSpec, String errorMessage, Application application) {
        Map<String, String> details = prepareBulkApplicationDeploymentDetailsMap(appInstance, application);
        if (Objects.isNull(appInstance)) {
            details.put(BULK_ENTRY_DETAIL_KEY_APP_INSTANCE_NAME, applicationSpec.getApplicationInstanceName());
            details.put(BULK_ENTRY_DETAIL_KEY_DOMAIN_NAME, applicationSpec.getDomainName());
        }
        details.put(BULK_ENTRY_DETAIL_KEY_ERROR_MESSAGE, errorMessage);
        return details;
    }

    private Map<String, String> prepareBulkApplicationDeploymentDetailsMap(AppInstance appInstance, Application application) {
        Map<String, String> details = new HashMap<>();
        if (Objects.nonNull(appInstance)) {
            details.put(BULK_ENTRY_DETAIL_KEY_APP_INSTANCE_ID, appInstance.getId().toString());
            details.put(BULK_ENTRY_DETAIL_KEY_APP_INSTANCE_NAME, appInstance.getName());
            details.put(BULK_ENTRY_DETAIL_KEY_DOMAIN_NAME, appInstance.getDomain().getName());
            details.put(BULK_ENTRY_DETAIL_KEY_DOMAIN_CODENAME, appInstance.getDomain().getCodename());
        }
        if (Objects.nonNull(application)) {
            details.put(BULK_ENTRY_DETAIL_KEY_APP_NAME, application.getName());
            details.put(BULK_ENTRY_DETAIL_KEY_APP_ID, String.valueOf(findApplicationBaseId(application.getName())));
        }
        return details;
    }

    private Long findApplicationBaseId(String applicationName) {
        return applicationBaseService.findByName(applicationName).getId();
    }

    private void logBulkStateUpdate(long bulkId, String state) {
        log.debug("State of bulk {} set to {}", bulkId, state);
    }

    public List<BulkAppDetails> getAppsBulkDetails(BulkDeploymentView bulkDeployment) {

        List<BulkAppDetails> result = new ArrayList<>();

        bulkDeployment.getEntries().forEach(deployment -> {
            Long instanceId = Long.valueOf(deployment.getDetails().get(BULK_ENTRY_DETAIL_KEY_APP_INSTANCE_ID));
            AppInstance instance = instanceService.find(instanceId).orElseThrow();

            Map<String, String> configurationParameters = new HashMap<>();

            //deploy
            Map<String, String> params = appDeploymentMonitor.appDeploymentParameters(instance.getInternalId());
            params.forEach((key, value) -> {
                configurationParameters.put(key, Objects.isNull(value) || Objects.equals(value, "") ? EMPTY_VALUE : value.replace("\"", ""));
                log.debug("Params = {} - {}", key, value);
            });

            Map<String, String> accessMethodParameters = new HashMap<>();
            if (appDeploymentMonitor.state(instance.getInternalId()) != AppLifecycleState.APPLICATION_DEPLOYMENT_FAILED) {
                appDeploymentMonitor.userAccessDetails(instance.getInternalId()).getServiceAccessMethods()
                        .forEach(accessMethod ->
                                accessMethodParameters.put(
                                        accessMethod.getName() + "." + accessMethod.getProtocol(),
                                        Objects.isNull(accessMethod.getUrl()) || Objects.equals(accessMethod.getUrl(), "") ? EMPTY_VALUE : accessMethod.getUrl())
                        );
            }

            BulkAppDetails details = BulkAppDetails.builder().userName(instance.getOwner().getUsername())
                    .appInstanceName(instance.getName())
                    .appName(instance.getApplication().getName())
                    .domainCodeName(instance.getDomain().getCodename())
                    .appVersion(instance.getApplication().getVersion())
                    .parameters(configurationParameters)
                    .accessMethod(accessMethodParameters)
                    .build();
            result.add(details);
        });

        result.forEach(x -> {
            log.debug("Deployment entry details: {} {} {} {} {} {} {}", x.getAppName(), x.getAppVersion(), x.getAppInstanceName(), x.getUserName(), x.getDomainCodeName(), x.getParameters(), x.getAccessMethod());
        });

        return result;
    }

    public InputStreamResource getInputStreamAppBulkDetails(List<BulkAppDetails> bulkDeploymentDetails) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(byteArrayOutputStream);
            CSVWriter csvWriter = new CSVWriter(writer);
            // add header row
            List<String> header = createHeaderRow(bulkDeploymentDetails);
            log.debug("Header row: {}", header);
            csvWriter.writeNext(header.toArray(new String[0]));

            // add data rows
            bulkDeploymentDetails.forEach(bulkDetails -> {
                List<String> valuesInOrder = new ArrayList<>();
                valuesInOrder.add(bulkDetails.getDomainCodeName());
                valuesInOrder.add(bulkDetails.getAppName());
                valuesInOrder.add(bulkDetails.getAppInstanceName());
                valuesInOrder.add(bulkDetails.getUserName());
                valuesInOrder.add(bulkDetails.getAppVersion());
                // access methods
                valuesInOrder.addAll(bulkDetails.getAccessMethod().values());
                // config parameters
                valuesInOrder.addAll(bulkDetails.getParameters().values());
                log.debug("Data row: {}", valuesInOrder);
                csvWriter.writeNext(valuesInOrder.toArray(new String[0]));
            });

            csvWriter.close();
            writer.close();
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            log.debug("Csv content size: {} bytes", byteArray.length);
            return new InputStreamResource(new ByteArrayInputStream(byteArray));

        } catch (Exception e) {
            log.error("Exception while preparing bulk deployment details CSV content", e);
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static List<String> createHeaderRow(List<BulkAppDetails> details) {
        // default column names
        List<String> header = new ArrayList<>(List.of("domainCodeName", "appName", "appInstanceName", "userName", "appVersion"));

        // access methods header
        header.addAll(details.get(0).getAccessMethod().keySet());

        // config parameters header
        details.get(0).getParameters().keySet().forEach(param -> {
            param = param.replace("\"", "");
            header.add(CSV_HEADER_PARAM_PREFIX + param);
        });

        return header;
    }

}
