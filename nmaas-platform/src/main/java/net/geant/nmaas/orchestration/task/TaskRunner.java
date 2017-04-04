package net.geant.nmaas.orchestration.task;

import net.geant.nmaas.orchestration.Identifier;
import org.springframework.context.ApplicationContext;

public class TaskRunner implements Runnable {

    private static final String APP_DEPLOYMENT_ORCHESTRATOR_TASK = "appDeploymentOrchestratorTask";

    private ApplicationContext context;

    private String taskName;

    private Identifier deploymentId;

    private Identifier clientId;

    private Identifier applicationId;

    public TaskRunner(ApplicationContext context, Identifier deploymentId, Identifier clientId, Identifier applicationId) {
        this.context = context;
        this.deploymentId = deploymentId;
        this.clientId = clientId;
        this.applicationId = applicationId;
        this.taskName = APP_DEPLOYMENT_ORCHESTRATOR_TASK;
    }

    @Override
    public void run() {
        switch(taskName) {
            case APP_DEPLOYMENT_ORCHESTRATOR_TASK:
                AppDeploymentOrchestratorTask deployment = (AppDeploymentOrchestratorTask) context.getBean(APP_DEPLOYMENT_ORCHESTRATOR_TASK);
                deployment.deploy(deploymentId, clientId, applicationId);
                break;
            default:
                throw new NullPointerException();
        }
    }
}
