package net.geant.nmaas.orchestration.tasks;

import net.geant.nmaas.dcn.deployment.DcnDeploymentProvider;
import net.geant.nmaas.dcn.deployment.DcnSpec;
import net.geant.nmaas.dcn.deployment.exceptions.DcnRequestVerificationException;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerContainerSpec;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerContainerTemplate;
import net.geant.nmaas.nmservice.deployment.exceptions.NmServiceRequestVerificationException;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceSpec;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.AppVerifyRequestActionEvent;
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
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class AppRequestVerificationTask {

    private final static Logger log = LogManager.getLogger(AppRequestVerificationTask.class);

    private NmServiceDeploymentProvider serviceDeployment;

    private DcnDeploymentProvider dcnDeployment;

    private AppDeploymentRepository repository;

    private ApplicationRepository appRepository;

    @Autowired
    public AppRequestVerificationTask(
            NmServiceDeploymentProvider serviceDeployment,
            DcnDeploymentProvider dcnDeployment,
            AppDeploymentRepository repository,
            ApplicationRepository appRepository) {
        this.serviceDeployment = serviceDeployment;
        this.dcnDeployment = dcnDeployment;
        this.repository = repository;
        this.appRepository = appRepository;
    }

    @Async
    @EventListener
    @Loggable(LogLevel.INFO)
    public void verifyAppRequest(AppVerifyRequestActionEvent event) throws InvalidDeploymentIdException, InvalidApplicationIdException {
        final Identifier deploymentId = event.getDeploymentId();
        AppDeployment appDeployment = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        final Identifier clientId = appDeployment.getClientId();
        final Identifier applicationId = appDeployment.getApplicationId();
        NmServiceInfo serviceInfo = null;
        try {
            serviceInfo = serviceDeployment.verifyRequest(deploymentId, constructNmServiceSpec(clientId, applicationId));
            dcnDeployment.verifyRequest(deploymentId, constructDcnSpec(clientId, applicationId, serviceInfo));
        } catch (NmServiceRequestVerificationException e) {
            log.warn("Service request verification failed for deployment " + deploymentId.value() + " -> " + e.getMessage());
        } catch (DcnRequestVerificationException e) {
            log.warn("DCN request verification failed for deployment " + deploymentId.value() + " -> " + e.getMessage());
        }
    }

    @Loggable(LogLevel.DEBUG)
    @Transactional
    NmServiceSpec constructNmServiceSpec(Identifier clientId, Identifier applicationId) throws InvalidApplicationIdException {
        final Application application = appRepository.findOne(Long.valueOf(applicationId.getValue()));
        if (application == null)
            throw new InvalidApplicationIdException("Application with id " + applicationId + " does not exist in repository");
        DockerContainerTemplate template = DockerContainerTemplate.copy(application.getDockerContainerTemplate());
        return new DockerContainerSpec(
                buildServiceName(application),
                template,
                Long.valueOf(clientId.getValue()));
    }

    @Loggable(LogLevel.DEBUG)
    String buildServiceName(Application application) {
        return application.getName() + "-" + application.getId();
    }

    @Loggable(LogLevel.DEBUG)
    DcnSpec constructDcnSpec(Identifier clientId, Identifier applicationId, NmServiceInfo serviceInfo) {
        DcnSpec dcn = new DcnSpec(buildDcnName(applicationId, clientId));
        if (serviceInfo != null && serviceInfo.getNetwork() != null)
            dcn.setNmServiceDeploymentNetworkDetails(serviceInfo.getNetwork());
        else
            log.warn("Failed to set NM service deployment network details in DCN spec");
        return dcn;
    }

    @Loggable(LogLevel.DEBUG)
    String buildDcnName(Identifier applicationId, Identifier clientId) {
        return clientId + "-" + applicationId;
    }

}
