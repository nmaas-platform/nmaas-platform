package net.geant.nmaas.orchestration.tasks.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.events.app.AppAutoDeploymentStatusUpdateEvent;
import net.geant.nmaas.orchestration.events.app.AppAutoDeploymentTriggeredEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Component
@RequiredArgsConstructor
@Log4j2
public class AppAutoDeploymentMonitorTask {

    private final AppDeploymentMonitor appDeploymentMonitor;
    private final AppLifecycleManager appLifecycleManager;

    @EventListener
    @Transactional
    @Loggable(LogLevel.INFO)
    public ApplicationEvent trigger(AppAutoDeploymentTriggeredEvent event) throws InterruptedException {
        try {
            AppLifecycleState appLifecycleState = appDeploymentMonitor.state(event.getDeploymentId());
            if (AppLifecycleState.DEPLOYMENT_ENVIRONMENT_PREPARED.equals(appLifecycleState)) {
                appLifecycleManager.applyConfiguration(event.getDeploymentId(), event.getAppConfigurationView(), null);
            } else {
                Thread.sleep(15000);
                return event;
            }
        } catch (InvalidDeploymentIdException e) {
            log.warn("App deployment with provided identifier doesn't exist ({})", event.getDeploymentId());
        }
        return new AppAutoDeploymentStatusUpdateEvent(this, event.getBulkDeploymentId(), event.getDeploymentId());
    }

}
