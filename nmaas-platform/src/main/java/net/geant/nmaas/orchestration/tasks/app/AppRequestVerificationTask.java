package net.geant.nmaas.orchestration.tasks.app;

import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerTemplate;
import net.geant.nmaas.nmservice.deployment.exceptions.NmServiceRequestVerificationException;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.app.AppVerifyRequestActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidApplicationIdException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AppRequestVerificationTask {

    private final static Logger log = LogManager.getLogger(AppRequestVerificationTask.class);

    private NmServiceDeploymentProvider serviceDeployment;

    private AppDeploymentRepository repository;

    private ApplicationRepository appRepository;

    @Autowired
    public AppRequestVerificationTask(
            NmServiceDeploymentProvider serviceDeployment,
            AppDeploymentRepository repository,
            ApplicationRepository appRepository) {
        this.serviceDeployment = serviceDeployment;
        this.repository = repository;
        this.appRepository = appRepository;
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void verifyAppRequest(AppVerifyRequestActionEvent event) throws InvalidDeploymentIdException, InvalidApplicationIdException {
        final Identifier deploymentId = event.getDeploymentId();
        final AppDeployment appDeployment = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        final Identifier clientId = appDeployment.getClientId();
        final Identifier applicationId = appDeployment.getApplicationId();
        try {
            serviceDeployment.verifyRequest(deploymentId, clientId, template(applicationId));
        } catch (NmServiceRequestVerificationException e) {
            log.warn("Service request verification failed for deployment " + deploymentId.value() + " -> " + e.getMessage());
        }
    }

    @Loggable(LogLevel.DEBUG)
    public DockerContainerTemplate template(Identifier applicationId) throws InvalidApplicationIdException {
        final Application application = appRepository.findOne(Long.valueOf(applicationId.getValue()));
        if (application == null)
            throw new InvalidApplicationIdException("Application with id " + applicationId + " does not exist in repository");
        return DockerContainerTemplate.copy(application.getAppDeploymentSpec().getDockerContainerTemplate());
    }

}
