package net.geant.nmaas.orchestration.tasks.app;

import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotPrepareEnvironmentException;
import net.geant.nmaas.orchestration.events.app.AppPrepareEnvironmentActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Log4j2
public class AppEnvironmentPreparationTask {

    private NmServiceDeploymentProvider serviceDeployment;

    private AppDeploymentRepository repository;

    @Autowired
    public AppEnvironmentPreparationTask(NmServiceDeploymentProvider serviceDeployment, AppDeploymentRepository repository) {
        this.serviceDeployment = serviceDeployment;
        this.repository = repository;
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public void trigger(AppPrepareEnvironmentActionEvent event) throws InvalidDeploymentIdException, CouldNotPrepareEnvironmentException {
        try {
            boolean configFileRepositoryRequired = repository.findByDeploymentId(event.getRelatedTo()).orElseThrow(InvalidDeploymentIdException::new).isConfigFileRepositoryRequired();
            serviceDeployment.prepareDeploymentEnvironment(event.getRelatedTo(), configFileRepositoryRequired);
        }catch(Exception ex){
            long timestamp = System.currentTimeMillis();
            log.error("Error reported at " + timestamp, ex);
        }
    }
}
