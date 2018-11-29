package net.geant.nmaas.orchestration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.configuration.exceptions.UserConfigHandlingException;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.repository.NmServiceInfoRepository;
import net.geant.nmaas.orchestration.api.model.AppConfigurationView;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.app.AppApplyConfigurationActionEvent;
import net.geant.nmaas.orchestration.events.app.AppRemoveActionEvent;
import net.geant.nmaas.orchestration.events.app.AppRestartActionEvent;
import net.geant.nmaas.orchestration.events.app.AppUpdateConfigurationEvent;
import net.geant.nmaas.orchestration.events.app.AppVerifyRequestActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Default {@link AppLifecycleManager} implementation.
 */
@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DefaultAppLifecycleManager implements AppLifecycleManager {

    private AppDeploymentRepositoryManager repositoryManager;

    private ApplicationEventPublisher eventPublisher;

    private NmServiceInfoRepository nmServiceInfoRepository;

    @Autowired
    public DefaultAppLifecycleManager(AppDeploymentRepositoryManager repositoryManager,
                                      ApplicationEventPublisher eventPublisher, NmServiceInfoRepository nmServiceInfoRepository) {
        this.repositoryManager = repositoryManager;
        this.eventPublisher = eventPublisher;
        this.nmServiceInfoRepository = nmServiceInfoRepository;
    }

    @Override
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Identifier deployApplication(AppDeployment appDeployment) {
        Identifier deploymentId = generateDeploymentId();
        appDeployment.setDeploymentId(deploymentId);
        repositoryManager.store(appDeployment);
        eventPublisher.publishEvent(new AppVerifyRequestActionEvent(this, deploymentId));
        return deploymentId;
    }

    Identifier generateDeploymentId() {
        Identifier generatedId;
        do {
            generatedId = new Identifier(UUID.randomUUID().toString());
        } while(deploymentDoesNotStartWithLetter(generatedId) || deploymentIdAlreadyInUse(generatedId));
        return generatedId;
    }

    private boolean deploymentDoesNotStartWithLetter(Identifier generatedId) {
        return !generatedId.value().matches("[a-z]([-a-z0-9]*[a-z0-9])?");
    }

    private boolean deploymentIdAlreadyInUse(Identifier generatedId) {
        return repositoryManager.load(generatedId).isPresent();
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void redeployApplication(Identifier deploymentId){
        eventPublisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.INIT, ""));
        eventPublisher.publishEvent(new AppVerifyRequestActionEvent(this, deploymentId));
    }

    @Override
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void applyConfiguration(Identifier deploymentId, AppConfigurationView configuration) throws Throwable {
        AppDeployment appDeployment = repositoryManager.load(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException("No application deployment with provided identifier found."));
        NmServiceInfo serviceInfo = (NmServiceInfo) nmServiceInfoRepository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException("No nm service info with provided identifier found."));
        appDeployment.setConfiguration(new AppConfiguration(configuration.getJsonInput()));
        if(configuration.getStorageSpace() != null){
            appDeployment.setStorageSpace(configuration.getStorageSpace());
            serviceInfo.setStorageSpace(configuration.getStorageSpace());
        }
        if(configuration.getAdditionalParameters() != null && !configuration.getAdditionalParameters().isEmpty()){
            if(serviceInfo.getAdditionalParameters() == null){
                serviceInfo.setAdditionalParameters(this.getMapFromJson(configuration.getAdditionalParameters()));
            } else {
                serviceInfo.getAdditionalParameters().putAll(this.getMapFromJson(configuration.getAdditionalParameters()));
            }
        }
        repositoryManager.update(appDeployment);
        nmServiceInfoRepository.save(serviceInfo);
        if(appDeployment.getState().equals(AppDeploymentState.MANAGEMENT_VPN_CONFIGURED)){
            eventPublisher.publishEvent(new AppApplyConfigurationActionEvent(this, deploymentId));
        }
        else if(appDeployment.getState().equals(AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFIED)){
            eventPublisher.publishEvent(new AppUpdateConfigurationEvent(this, deploymentId));
        }
    }

    private Map<String, String> getMapFromJson(String inputJson){
        try {
            return new ObjectMapper().readValue(inputJson, new TypeReference<Map<String, String>>() {});
        } catch (IOException e) {
            throw new UserConfigHandlingException("Wasn't able to map additional parameters to model map -> " + e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeApplication(Identifier deploymentId) {
        eventPublisher.publishEvent(new AppRemoveActionEvent(this, deploymentId));
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void updateApplication(Identifier deploymentId, Identifier applicationId) {
        throw new NotImplementedException();
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void updateConfiguration(Identifier deploymentId, AppConfiguration configuration) {
        throw new NotImplementedException();
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void restartApplication(Identifier deploymentId) {
        eventPublisher.publishEvent(new AppRestartActionEvent(this, deploymentId));
    }
}
