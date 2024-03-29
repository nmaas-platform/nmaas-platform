package net.geant.nmaas.orchestration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.configuration.exceptions.UserConfigHandlingException;
import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.janitor.JanitorService;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.orchestration.api.model.AppConfigurationView;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.events.app.AppApplyConfigurationActionEvent;
import net.geant.nmaas.orchestration.events.app.AppRemoveActionEvent;
import net.geant.nmaas.orchestration.events.app.AppRemoveFailedActionEvent;
import net.geant.nmaas.orchestration.events.app.AppRestartActionEvent;
import net.geant.nmaas.orchestration.events.app.AppUpgradeActionEvent;
import net.geant.nmaas.orchestration.events.app.AppVerifyRequestActionEvent;
import net.geant.nmaas.orchestration.events.app.AppVerifyServiceActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Default {@link AppLifecycleManager} implementation.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class DefaultAppLifecycleManager implements AppLifecycleManager {

    private final AppDeploymentRepositoryManager deploymentRepositoryManager;
    private final ApplicationEventPublisher eventPublisher;
    private final NmServiceRepositoryManager serviceRepositoryManager;
    private final JanitorService janitorService;

    private final AppTermsAcceptanceService appTermsAcceptanceService;

    @Override
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Identifier deployApplication(AppDeployment appDeployment) {
        Identifier deploymentId = generateDeploymentId();
        appDeployment.setDeploymentId(deploymentId);
        deploymentRepositoryManager.store(appDeployment);
        eventPublisher.publishEvent(new AppVerifyRequestActionEvent(this, deploymentId));
        return deploymentId;
    }

    Identifier generateDeploymentId() {
        Identifier generatedId;
        do {
            generatedId = new Identifier(UUID.randomUUID().toString());
        } while (deploymentDoesNotStartWithLetter(generatedId) || deploymentIdAlreadyInUse(generatedId));
        return generatedId;
    }

    private boolean deploymentDoesNotStartWithLetter(Identifier generatedId) {
        return !generatedId.value().matches("[a-z]([-a-z0-9]*[a-z0-9])?");
    }

    private boolean deploymentIdAlreadyInUse(Identifier generatedId) {
        try {
            deploymentRepositoryManager.load(generatedId);
        } catch (InvalidDeploymentIdException e) {
            return false;
        }
        return true;
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void redeployApplication(Identifier deploymentId) {
        eventPublisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.INIT, ""));
        eventPublisher.publishEvent(new AppVerifyRequestActionEvent(this, deploymentId));
    }

    @SuppressWarnings("unchecked")
    @Override
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void applyConfiguration(Identifier deploymentId, AppConfigurationView configuration, String initiator) {
        AppDeployment appDeployment = deploymentRepositoryManager.load(deploymentId);
        if (appDeployment.getConfiguration() != null) {
            appDeployment.getConfiguration().setJsonInput(configuration.getJsonInput());
        } else {
            appDeployment.setConfiguration(new AppConfiguration(configuration.getJsonInput()));
        }
        if (configuration.getStorageSpace() != null) {
            serviceRepositoryManager.updateStorageSpace(deploymentId, configuration.getStorageSpace());
        }
        if (isNotEmpty(configuration.getAdditionalParameters())) {
            serviceRepositoryManager.addAdditionalParameters(
                    deploymentId,
                    replaceHashWithDotInMapKeysAndProcessValues(getMapFromJson(configuration.getAdditionalParameters())));
        }
        if (isNotEmpty(configuration.getMandatoryParameters())) {
            serviceRepositoryManager.addAdditionalParameters(
                    deploymentId,
                    replaceHashWithDotInMapKeysAndProcessValues(getMapFromJson(configuration.getMandatoryParameters())));
        }
        if (isNotEmpty(configuration.getAccessCredentials())) {
            changeBasicAuth(
                    appDeployment.getDescriptiveDeploymentId(),
                    serviceRepositoryManager.loadDomain(deploymentId),
                    configuration.getAccessCredentials());
        }
        /*
         * NMAAS-967
         * if terms acceptance is required, perform check actions
         */
        if (appDeployment.isTermsAcceptanceRequired()) {
            if (!isNotEmpty(configuration.getTermsAcceptance())) {
                // Terms are empty
                log.error("Terms acceptance is required for this application, however terms are not present in user configuration data");
                throw new ProcessingException("Terms acceptance is required, however terms are not present");
            }
            Map<String, String> termsAcceptanceMap = replaceHashWithDotInMapKeysAndProcessValues(getMapFromJson(configuration.getTermsAcceptance()));
            String termsContent = termsAcceptanceMap.get("termsContent");
            // TODO validate terms content
            String termsAcceptanceStatement = termsAcceptanceMap.get("termsAcceptanceStatement");

            OffsetDateTime now = OffsetDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());

            if (termsAcceptanceStatement != null && termsAcceptanceStatement.equalsIgnoreCase("yes")) {
                // OK
                log.info(String.format(
                        "Application usage terms were accepted: application [%s], instance id [%s], content [%s], statement [%s], by [%s], at: [%s]",
                        appDeployment.getAppName(),
                        appDeployment.getInstanceId(),
                        termsContent,
                        termsAcceptanceStatement,
                        initiator,
                        now.format(DateTimeFormatter.ISO_DATE_TIME)
                ));
                appTermsAcceptanceService.addTermsAcceptanceEntry(
                        appDeployment.getAppName(),
                        appDeployment.getInstanceId(),
                        initiator,
                        termsContent,
                        termsAcceptanceStatement,
                        now
                );
            } else {
                // Terms were not accepted by they should
                throw new ProcessingException("Application usage terms acceptance is required, however terms were not accepted");
            }

        }
        deploymentRepositoryManager.update(appDeployment);
        if (appDeployment.getState().equals(AppDeploymentState.MANAGEMENT_VPN_CONFIGURED)) {
            eventPublisher.publishEvent(new AppApplyConfigurationActionEvent(this, deploymentId));
        }
    }

    Map<String, String> getMapFromJson(String inputJson) {
        try {
            return new ObjectMapper().readValue(inputJson, new TypeReference<Map<String, String>>() { });
        } catch (IOException e) {
            throw new UserConfigHandlingException("Wasn't able to map additional parameters to model map -> " + e.getMessage());
        }
    }

    static Map<String, String> replaceHashWithDotInMapKeysAndProcessValues(Map<String, String> map) {
        Map<String, String> newMap = new HashMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                newMap.put(
                        entry.getKey().replace("#", "."),
                        escapeCommasIfRequired(
                                addQuotationMarkIfRequired(
                                        replaceHashWithQuote(entry.getValue())))
                );
            }
        }
        return newMap;
    }

    private static String replaceHashWithQuote(String value) {
        return value.replace("#", "\\\"");
    }

    private static String addQuotationMarkIfRequired(String value) {
        return value.contains(" ") ? "\"" + value + "\"" : value;
    }

    private static String escapeCommasIfRequired(String value) {
        return value.replace(",", "\\,");
    }

    private void changeBasicAuth(Identifier deploymentId, String domain, String accessCredentials) {
        Map<String, String> accessCredentialsMap = this.getMapFromJson(accessCredentials);
        String basicAuthUsername = accessCredentialsMap.get("accessUsername");
        String basicAuthPassword = accessCredentialsMap.get("accessPassword");
        if (isNotEmpty(basicAuthUsername) && isNotEmpty(basicAuthPassword)) {
            janitorService.createOrReplaceBasicAuth(deploymentId, domain, basicAuthUsername, basicAuthPassword);
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeApplication(Identifier deploymentId) {
        if (!AppDeploymentState.APPLICATION_REMOVED.equals(deploymentRepositoryManager.loadState(deploymentId))) {
            eventPublisher.publishEvent(new AppRemoveActionEvent(this, deploymentId));
        }
    }

    @Override
    @Loggable(LogLevel.DEBUG)
    public void removeFailedApplication(Identifier deploymentId) {
        eventPublisher.publishEvent(new AppRemoveFailedActionEvent(this, deploymentId));
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void upgradeApplication(Identifier deploymentId, Identifier targetApplicationId) {
        if (AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFIED.equals(deploymentRepositoryManager.loadState(deploymentId))) {
            eventPublisher.publishEvent(new AppUpgradeActionEvent(this, deploymentId, targetApplicationId, AppUpgradeMode.MANUAL));
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateConfiguration(Identifier deploymentId, AppConfigurationView configuration) {
        AppDeployment appDeployment = deploymentRepositoryManager.load(deploymentId);
        // only access credentials update is currently supported
        if (isNotEmpty(configuration.getAccessCredentials())) {
            changeBasicAuth(appDeployment.getDescriptiveDeploymentId(), appDeployment.getDomain(), configuration.getAccessCredentials());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void restartApplication(Identifier deploymentId) {
        eventPublisher.publishEvent(new AppRestartActionEvent(this, deploymentId));
    }

    @Override
    public void updateApplicationStatus(Identifier deploymentId) {
        eventPublisher.publishEvent(new AppVerifyServiceActionEvent(this, deploymentId));
    }

}
