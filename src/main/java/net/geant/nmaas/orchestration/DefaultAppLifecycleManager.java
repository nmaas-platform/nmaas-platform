package net.geant.nmaas.orchestration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.configuration.exceptions.UserConfigHandlingException;
import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState;
import net.geant.nmaas.orchestration.api.model.AppConfigurationView;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.events.app.AppApplyConfigurationActionEvent;
import net.geant.nmaas.orchestration.events.app.AppRemoveActionEvent;
import net.geant.nmaas.orchestration.events.app.AppRemoveFailedActionEvent;
import net.geant.nmaas.orchestration.events.app.AppRestartActionEvent;
import net.geant.nmaas.orchestration.events.app.AppUpdateBasicAuthActionEvent;
import net.geant.nmaas.orchestration.events.app.AppUpgradeActionEvent;
import net.geant.nmaas.orchestration.events.app.AppVerifyRequestActionEvent;
import net.geant.nmaas.orchestration.events.app.AppVerifyServiceActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Default {@link AppLifecycleManager} implementation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultAppLifecycleManager implements AppLifecycleManager {

    public static final String TAG_DOT = "_dot_";
    public static final String TAG_BRACKET = "_bracket_";

    private final AppDeploymentRepositoryManager deploymentRepositoryManager;
    private final ApplicationEventPublisher eventPublisher;
    private final NmServiceRepositoryManager serviceRepositoryManager;

    private final AppTermsAcceptanceService appTermsAcceptanceService;
    private final ConfigurationManager configurationManager;
    private final JsonMapper jsonMapper;

    @Value("${nmaas.platform.multi-instance}")
    private boolean useDeploymentPrefix;

    @Override
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Identifier deployApplication(AppDeployment appDeployment) {
        Identifier deploymentId = generateDeploymentId();
        appDeployment.setDeploymentId(deploymentId);
        if (useDeploymentPrefix) {
            appDeployment.setDeploymentName(configurationManager.getConfiguration().getDeploymentPrefix() + "-" + appDeployment.getDeploymentName());
        }
        deploymentRepositoryManager.store(appDeployment);
        eventPublisher.publishEvent(new AppVerifyRequestActionEvent(this, deploymentId));
        return deploymentId;
    }

    @Override
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Identifier initApplicationDeployment(AppDeployment appDeployment) {
        Identifier deploymentId = generateDeploymentId();
        appDeployment.setDeploymentId(deploymentId);
        if (useDeploymentPrefix) {
            appDeployment.setDeploymentName(configurationManager.getConfiguration().getDeploymentPrefix() + "-" + appDeployment.getDeploymentName());
        }
        deploymentRepositoryManager.store(appDeployment);
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
        eventPublisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, ServiceDeploymentState.INIT, ""));
        eventPublisher.publishEvent(new AppVerifyRequestActionEvent(this, deploymentId));
    }

    @SuppressWarnings("unchecked")
    @Override
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void applyConfiguration(Identifier deploymentId, AppConfigurationView configuration, String initiator) {
        final AppDeployment appDeployment = deploymentRepositoryManager.load(deploymentId);
        verifyTermsAcceptanceIfRequired(configuration, initiator, appDeployment);
        Map<String, String> additionalParameters = preprocessParameters(configuration.getAdditionalParameters());
        Map<String, String> mandatoryParameters = preprocessParameters(configuration.getMandatoryParameters());
        Map<String, String> accessCredentials = preprocessParameters(configuration.getAccessCredentials());

        if (appDeployment.getConfiguration() != null) {
            appDeployment.getConfiguration().setJsonInput(jsonMapper.writeValueAsString(configuration.getJsonInput()));
        } else {
            appDeployment.setConfiguration(new AppConfiguration(jsonMapper.writeValueAsString(configuration.getJsonInput())));
        }
        if (configuration.getStorageSpace() != null) {
            serviceRepositoryManager.updateStorageSpace(deploymentId, configuration.getStorageSpace());
        }
        if (!additionalParameters.isEmpty()) {
            serviceRepositoryManager.addAdditionalParameters(deploymentId, additionalParameters);
        }
        if (!mandatoryParameters.isEmpty()) {
            serviceRepositoryManager.addAdditionalParameters(deploymentId, mandatoryParameters);
        }
        if (!accessCredentials.isEmpty()) {
            triggerBasicAuthUpdate(deploymentId, accessCredentials);
        }
        deploymentRepositoryManager.update(appDeployment);

        if (appDeployment.getState().equals(AppDeploymentState.MANAGEMENT_VPN_CONFIGURED)) {
            eventPublisher.publishEvent(new AppApplyConfigurationActionEvent(this, deploymentId));
        }
    }

    private void verifyTermsAcceptanceIfRequired(AppConfigurationView configuration, String initiator, AppDeployment appDeployment) {
        if (appDeployment.isTermsAcceptanceRequired()) {
            if (configuration.getTermsAcceptance() == null || configuration.getTermsAcceptance().isNull()) {
                log.error("Terms acceptance is required for this application, however terms are not present in user configuration data");
                throw new ProcessingException("Terms acceptance is required, however terms are not present");
            }
            Map<String, String> termsAcceptanceMap = preprocessParameters(configuration.getTermsAcceptance());
            String termsContent = termsAcceptanceMap.get("termsContent");

            // TODO validate terms content
            String termsAcceptanceStatement = termsAcceptanceMap.get("termsAcceptanceStatement");
            if (termsAcceptanceStatement != null && termsAcceptanceStatement.equalsIgnoreCase("yes")) {
                // OK
                log.info("Application usage terms were accepted: application [{}], instance id [{}], content [{}], statement [{}], by [{}], at: [{}]",
                        appDeployment.getAppName(),
                        appDeployment.getInstanceId(),
                        termsContent,
                        termsAcceptanceStatement,
                        initiator,
                        OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
                appTermsAcceptanceService.addTermsAcceptanceEntry(
                        appDeployment.getAppName(),
                        appDeployment.getInstanceId(),
                        initiator,
                        termsContent,
                        termsAcceptanceStatement,
                        OffsetDateTime.now()
                );
            } else {
                // Terms were not accepted by they should
                throw new ProcessingException("Application usage terms acceptance is required, however terms were not accepted");
            }
        }
    }

    private void triggerBasicAuthUpdate(Identifier deploymentId, Map<String, String> accessCredentialsMap) {
        final String basicAuthUsername = accessCredentialsMap.get("accessUsername");
        final String basicAuthPassword = accessCredentialsMap.get("accessPassword");
        if (isNotEmpty(basicAuthUsername) && isNotEmpty(basicAuthPassword)) {
            eventPublisher.publishEvent(new AppUpdateBasicAuthActionEvent(this, deploymentId, basicAuthUsername, basicAuthPassword));
        } else {
            log.warn("Missing access credentials for basic auth");
        }
    }

    Map<String, String> getMapFromJson(String inputJson) {
        if (isEmpty(inputJson) || "null".equals(inputJson)) {
            return Collections.emptyMap();
        }
        try {
            Map<String, String> mappedParameters = jsonMapper.readValue(inputJson, new TypeReference<Map<String, String>>() {
            });
            return mappedParameters != null ? mappedParameters : Collections.emptyMap();
        } catch (JacksonException e) {
            throw new UserConfigHandlingException("Wasn't able to map additional parameters to model map -> " + e.getMessage());
        }
    }

    Map<String, String> preprocessParameters(JsonNode parameters) {
        if (parameters == null || parameters.isNull()) {
            return Collections.emptyMap();
        }
        return preprocessParameters(getMapFromJson(jsonMapper.writeValueAsString(parameters)));
    }

    static Map<String, String> preprocessParameters(Map<String, String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> newMap = new HashMap<>();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (StringUtils.isNotEmpty(entry.getValue())) {
                newMap.put(processParamName(entry.getKey()), processParamValue(entry.getValue()));
            }
        }
        return newMap;
    }

    private static String processParamName(String name) {
        String processed = name.replace(TAG_DOT, ".");
        if (countBrackets(name) == 2) {
            processed = processed.replaceFirst(TAG_BRACKET, "[").replaceFirst(TAG_BRACKET, "]");
        }
        return processed;
    }

    public static int countBrackets(String text) {
        Matcher m = Pattern.compile(Pattern.quote(TAG_BRACKET)).matcher(text);
        int count = 0;
        while (m.find()) count++;
        return count;
    }

    private static String processParamValue(String value) {
        return escapeCommasIfRequired(
                addQuotationMarkIfRequired(
                        replaceHashWithQuote(value)));
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

    @Override
    @Loggable(LogLevel.INFO)
    public void removeApplication(Identifier deploymentId) {
        try {
            if (!AppDeploymentState.APPLICATION_REMOVED.equals(deploymentRepositoryManager.loadState(deploymentId))) {
                eventPublisher.publishEvent(new AppRemoveActionEvent(this, deploymentId));
            }
        } catch (InvalidDeploymentIdException e) {
            log.warn("Application deployment {} not found for removal. Skipping.", deploymentId, e);
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
        // only access credentials update is currently supported
        Map<String, String> accessCredentials = preprocessParameters(configuration.getAccessCredentials());
        if (!accessCredentials.isEmpty()) {
            triggerBasicAuthUpdate(deploymentId, accessCredentials);
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
