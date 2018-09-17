package net.geant.nmaas.orchestration;

import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.app.AppApplyConfigurationActionEvent;
import net.geant.nmaas.orchestration.events.app.AppRemoveActionEvent;
import net.geant.nmaas.orchestration.events.app.AppRestartActionEvent;
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

import java.util.UUID;

/**
 * Default {@link AppLifecycleManager} implementation.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DefaultAppLifecycleManager implements AppLifecycleManager {

    private AppDeploymentRepositoryManager repositoryManager;

    private ApplicationEventPublisher eventPublisher;

    @Autowired
    public DefaultAppLifecycleManager(AppDeploymentRepositoryManager repositoryManager, ApplicationEventPublisher eventPublisher) {
        this.repositoryManager = repositoryManager;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Identifier deployApplication(String domain, Identifier applicationId, String deploymentName, boolean configFileRepositoryRequired) {
        Identifier deploymentId = generateDeploymentId();
        AppDeployment appDeployment = new AppDeployment(deploymentId, domain, applicationId, deploymentName, configFileRepositoryRequired);
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
        eventPublisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.INIT));
        eventPublisher.publishEvent(new AppVerifyRequestActionEvent(this, deploymentId));
    }

    @Override
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void applyConfiguration(Identifier deploymentId, AppConfiguration configuration) throws InvalidDeploymentIdException {
        AppDeployment appDeployment = repositoryManager.load(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException("No application deployment with provided identifier found."));
        throwExceptionIfInInvalidState(appDeployment);
        appDeployment.setConfiguration(configuration);
        repositoryManager.update(appDeployment);
        eventPublisher.publishEvent(new AppApplyConfigurationActionEvent(this, deploymentId));
    }

    private void throwExceptionIfInInvalidState(AppDeployment appDeployment) throws InvalidDeploymentIdException {
        if (!appDeployment.getState().equals(AppDeploymentState.MANAGEMENT_VPN_CONFIGURED))
            throw new InvalidDeploymentIdException("Not able to apply configuration in current application deployment state -> " + appDeployment.getState());
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeApplication(Identifier deploymentId) throws InvalidDeploymentIdException {
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
    public void restartApplication(Identifier deploymentId) throws InvalidDeploymentIdException {
        eventPublisher.publishEvent(new AppRestartActionEvent(this, deploymentId));
    }
}
