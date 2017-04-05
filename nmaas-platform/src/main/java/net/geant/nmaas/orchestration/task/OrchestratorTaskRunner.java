package net.geant.nmaas.orchestration.task;

import net.geant.nmaas.orchestration.AppConfiguration;
import net.geant.nmaas.orchestration.Identifier;
import org.springframework.context.ApplicationContext;

public class OrchestratorTaskRunner implements Runnable {

    private static final String APP_DEPLOYMENT_ORCHESTRATOR_TASK = "appDeploymentOrchestratorTask";

    private static final String APP_CONFIGURATION_ORCHESTRATOR_TASK = "appConfigurationOrchestratorTask";

    private static final String APP_REMOVAL_ORCHESTRATOR_TASK = "appRemovalOrchestratorTask";

    private ApplicationContext context;

    private String taskName;

    private Identifier deploymentId;

    private Identifier clientId;

    private Identifier applicationId;

    private AppConfiguration appConfiguration;

    public OrchestratorTaskRunner(ApplicationContext context, Identifier deploymentId, Identifier clientId, Identifier applicationId) {
        this.context = context;
        this.deploymentId = deploymentId;
        this.clientId = clientId;
        this.applicationId = applicationId;
        this.taskName = APP_DEPLOYMENT_ORCHESTRATOR_TASK;
    }

    public OrchestratorTaskRunner(ApplicationContext context, Identifier deploymentId, AppConfiguration appConfiguration) {
        this.context = context;
        this.deploymentId = deploymentId;
        this.appConfiguration = appConfiguration;
        this.taskName = APP_CONFIGURATION_ORCHESTRATOR_TASK;
    }

    public OrchestratorTaskRunner(ApplicationContext context, Identifier deploymentId) {
        this.context = context;
        this.deploymentId = deploymentId;
        this.taskName = APP_REMOVAL_ORCHESTRATOR_TASK;
    }

    @Override
    public void run() {
        switch(taskName) {
            case APP_DEPLOYMENT_ORCHESTRATOR_TASK:
                AppDeploymentOrchestratorTask deployment = (AppDeploymentOrchestratorTask) context.getBean(APP_DEPLOYMENT_ORCHESTRATOR_TASK);
                deployment.deploy(deploymentId, clientId, applicationId);
                break;
            case APP_CONFIGURATION_ORCHESTRATOR_TASK:
                AppConfigurationOrchestratorTask configuration = (AppConfigurationOrchestratorTask) context.getBean(APP_CONFIGURATION_ORCHESTRATOR_TASK);
                configuration.configure(deploymentId, appConfiguration);
                break;
            case APP_REMOVAL_ORCHESTRATOR_TASK:
                AppRemovalOrchestratorTask removal = (AppRemovalOrchestratorTask) context.getBean(APP_REMOVAL_ORCHESTRATOR_TASK);
                removal.remove(deploymentId);
                break;
            default:
                throw new NullPointerException();
        }
    }
}
