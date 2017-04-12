package net.geant.nmaas.orchestration.tasks;

import net.geant.nmaas.dcn.deployment.DcnDeploymentProvider;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotPrepareDcnException;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotPrepareEnvironmentException;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.AppPrepareEnvironmentActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AppEnvironmentPreparationTask {

    private final static Logger log = LogManager.getLogger(AppEnvironmentPreparationTask.class);

    private NmServiceDeploymentProvider serviceDeployment;

    private DcnDeploymentProvider dcnDeployment;

    private AppDeploymentRepository repository;

    @Autowired
    public AppEnvironmentPreparationTask(
            NmServiceDeploymentProvider serviceDeployment,
            DcnDeploymentProvider dcnDeployment,
            AppDeploymentRepository repository) {
        this.serviceDeployment = serviceDeployment;
        this.dcnDeployment = dcnDeployment;
        this.repository = repository;
    }

    @Async
    @EventListener
    public void prepareEnvironment(AppPrepareEnvironmentActionEvent event) throws InvalidDeploymentIdException {
        final Identifier deploymentId = event.getDeploymentId();
        try {
            serviceDeployment.prepareDeploymentEnvironment(deploymentId);
            dcnDeployment.prepareDeploymentEnvironment(deploymentId);
        } catch (CouldNotPrepareEnvironmentException e) {
            log.warn("Service environment preparation failed for deployment " + deploymentId.value() + " -> " + e.getMessage());
        } catch (CouldNotPrepareDcnException e) {
            log.warn("DCN preparation failed for deployment " + deploymentId.value() + " -> " + e.getMessage());
        }
    }
}
