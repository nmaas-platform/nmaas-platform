package net.geant.nmaas.orchestration.tasks.app;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
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
    private final NmServiceDeploymentProvider serviceDeployment;


    @EventListener
    public void handleScaleEvent(AppScaleActionEvent event) {
        AppDeployment appDeployment = appDeploymentRepositoryManager.load(event.getDeploymentId());

        switch (event.getDirection()) {
            case DOWN:
                appDeployment.setState(AppDeploymentState.SCALED_DOWN);
                appDeploymentRepositoryManager.update(appDeployment);
                serviceDeployment.scaleDown(event.getDeploymentId());

                break;
            case UP:
                appDeployment.setState(AppDeploymentState.APPLICATION_CONFIGURED);
                appDeploymentRepositoryManager.update(appDeployment);
                serviceDeployment.scaleUp(event.getDeploymentId());

                break;
        }
    }
}
