package net.geant.nmaas.orchestration.tasks.app;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KServiceOperationsManager;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.events.app.AppScaleActionEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppScaleTask {

    private final AppDeploymentRepositoryManager appDeploymentRepositoryManager;
    private final KServiceOperationsManager kserviceOperationsManager;

    @EventListener
    public void handleScaleEvent(AppScaleActionEvent event) {

        AppDeployment appDeployment = appDeploymentRepositoryManager.load(event.getDeploymentId());

        switch (event.getDirection()) {
            case DOWN:
                appDeployment.setState(AppDeploymentState.SCALED_DOWN);
                appDeploymentRepositoryManager.update(appDeployment);

                kserviceOperationsManager.scaleDeployment(event.getDeploymentId(), 0);

                break;
            case UP:
                appDeployment.setState(AppDeploymentState.APPLICATION_CONFIGURED);
                appDeploymentRepositoryManager.update(appDeployment);

                kserviceOperationsManager.scaleDeployment(event.getDeploymentId(), 1);

                break;
        }
    }
}
