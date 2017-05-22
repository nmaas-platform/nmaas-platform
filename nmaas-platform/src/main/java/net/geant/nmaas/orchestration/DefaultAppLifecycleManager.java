package net.geant.nmaas.orchestration;

import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.app.AppApplyConfigurationActionEvent;
import net.geant.nmaas.orchestration.events.app.AppRemoveActionEvent;
import net.geant.nmaas.orchestration.events.app.AppVerifyRequestActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class DefaultAppLifecycleManager implements AppLifecycleManager {

    @Autowired
    private AppDeploymentRepositoryManager repositoryManager;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Identifier deployApplication(Identifier clientId, Identifier applicationId) {
        Identifier deploymentId = generateDeploymentId();
        AppDeployment appDeployment = new AppDeployment(deploymentId, clientId, applicationId);
        repositoryManager.store(appDeployment);
        eventPublisher.publishEvent(new AppVerifyRequestActionEvent(this, deploymentId));
        return deploymentId;
    }

    Identifier generateDeploymentId() {
        Identifier generatedId;
        do {
            generatedId = new Identifier(UUID.randomUUID().toString());
        } while(deploymentIdAlreadyInUse(generatedId));
        return generatedId;
    }

    boolean deploymentIdAlreadyInUse(Identifier generatedId) {
        return repositoryManager.load(generatedId).isPresent();
    }

    @Override
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void applyConfiguration(Identifier deploymentId, AppConfiguration configuration) throws InvalidDeploymentIdException {
        AppDeployment appDeployment = repositoryManager.load(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException("No application deployment with provided identifier found."));
        throwExceptionIfInInvalidState(appDeployment);
        appDeployment.setConfiguration(configuration);
        repositoryManager.store(appDeployment);
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

}
