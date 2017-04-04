package net.geant.nmaas.orchestration;

import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.task.AppConfigurationOrchestratorTask;
import net.geant.nmaas.orchestration.task.AppRemovalOrchestratorTask;
import net.geant.nmaas.orchestration.task.TaskRunner;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class DefaultAppLifecycleManager implements AppLifecycleManager {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private AppLifecycleRepository stateRepository;

    @Autowired
    private DeploymentIdToApplicationIdMapper deploymentIdToApplicationIdMapper;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Override
    @Loggable(LogLevel.INFO)
    public Identifier deployApplication(Identifier clientId, Identifier applicationId) {
        Identifier deploymentId = generateDeploymentId();
        stateRepository.storeNewDeployment(deploymentId);
        deploymentIdToApplicationIdMapper.storeMapping(deploymentId, applicationId);
        new Thread(new TaskRunner(context, deploymentId, clientId, applicationId)).start();
        return deploymentId;
    }

    private Identifier generateDeploymentId() {
        Identifier generatedId;
        do {
            generatedId = new Identifier(UUID.randomUUID().toString());
        } while(stateRepository.isDeploymentStored(generatedId));
        return generatedId;
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void applyConfiguration(Identifier deploymentId, AppConfiguration configuration) throws InvalidDeploymentIdException {
        setApplicationIdIfNotProvided(deploymentId, configuration);
        AppConfigurationOrchestratorTask configurationTask = (AppConfigurationOrchestratorTask) context.getBean("appConfigurationOrchestratorTask");
        configurationTask.populateProperties(deploymentId, configuration);
        taskExecutor.execute(configurationTask);
    }

    private void setApplicationIdIfNotProvided(Identifier deploymentId, AppConfiguration configuration) throws InvalidDeploymentIdException {
        try {
            if (applicationIdNotProvided(configuration))
                configuration.setApplicationId(deploymentIdToApplicationIdMapper.applicationId(deploymentId));
        } catch (DeploymentIdToApplicationIdMapper.EntryNotFoundException e) {
            throw new InvalidDeploymentIdException();
        }
    }

    private boolean applicationIdNotProvided(AppConfiguration configuration) {
        return configuration.getApplicationId() == null;
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeApplication(Identifier deploymentId) throws InvalidDeploymentIdException {
        AppRemovalOrchestratorTask removalTask = (AppRemovalOrchestratorTask) context.getBean("appRemovalOrchestratorTask");
        removalTask.populateIdentifiers(deploymentId);
        taskExecutor.execute(removalTask);
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void updateApplication(Identifier deploymentId, Identifier applicationId) {

    }

    @Override
    @Loggable(LogLevel.INFO)
    public void updateConfiguration(Identifier deploymentId, AppConfiguration configuration) {

    }

}
