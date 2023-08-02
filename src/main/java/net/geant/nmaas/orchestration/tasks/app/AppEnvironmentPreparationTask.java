package net.geant.nmaas.orchestration.tasks.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.orchestration.events.app.AppPrepareEnvironmentActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class AppEnvironmentPreparationTask {

    private final NmServiceDeploymentProvider serviceDeployment;
    private final AppDeploymentRepository repository;

    @EventListener
    @Loggable(LogLevel.INFO)
    public void trigger(AppPrepareEnvironmentActionEvent event) {
        try {
            boolean configFileRepositoryRequired = repository.findByDeploymentId(event.getRelatedTo()).orElseThrow(InvalidDeploymentIdException::new).isConfigFileRepositoryRequired();
            serviceDeployment.prepareDeploymentEnvironment(event.getRelatedTo(), configFileRepositoryRequired);
        } catch(Exception ex) {
            long timestamp = System.currentTimeMillis();
            log.error("Error reported at " + timestamp, ex);
        }
    }
}
