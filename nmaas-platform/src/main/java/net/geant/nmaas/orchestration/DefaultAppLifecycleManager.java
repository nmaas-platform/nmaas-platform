package net.geant.nmaas.orchestration;

import net.geant.nmaas.nmservice.deployment.repository.NmServiceTemplateRepository;
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
    private NmServiceTemplateRepository templates;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Override
    public Identifier deployApplication(Identifier clientId, Identifier applicationId) {
        Identifier deploymentId = generateDeploymentId();
        stateRepository.storeNewDeployment(deploymentId);
        AppDeploymentOrchestratorTask deployment = (AppDeploymentOrchestratorTask) context.getBean("appDeploymentOrchestratorTask");
        deployment.populateIdentifiers(deploymentId, clientId, applicationId);
        taskExecutor.execute(deployment);
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
    public void applyConfiguration(Identifier deploymentId, AppConfiguration configuration) {
        AppConfigurationOrchestratorTask configurationTask = (AppConfigurationOrchestratorTask) context.getBean("appConfigurationOrchestratorTask");
        configurationTask.populateProperties(deploymentId, configuration);
        taskExecutor.execute(configurationTask);
    }

    @Override
    public void removeApplication(Identifier deploymentId) {

    }

    @Override
    public void updateApplication(Identifier deploymentId, Identifier applicationId) {

    }

    @Override
    public void updateConfiguration(Identifier deploymentId, AppConfiguration configuration) {

    }

}
