package net.geant.nmaas.orchestration.task;

import net.geant.nmaas.dcn.deployment.DcnSpec;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerContainerSpec;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerContainerTemplate;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceSpec;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidApplicationIdException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class AppDeploymentOrchestratorTaskHelper {

    private final static Logger log = LogManager.getLogger(AppDeploymentOrchestratorTaskHelper.class);

    private ApplicationRepository applications;

    @Autowired
    public AppDeploymentOrchestratorTaskHelper(ApplicationRepository applications) {
        this.applications = applications;
    }

    @Loggable(LogLevel.DEBUG)
    public void verifyIfAllIdentifiersAreSet(Identifier deploymentId, Identifier clientId, Identifier applicationId) throws Exception {
        if (deploymentId == null || clientId == null || applicationId == null)
            throw new Exception("Input parameters verification failed (" + deploymentId + " and " + clientId + " and " + applicationId + ")");
    }

    @Loggable(LogLevel.DEBUG)
    @Transactional
    public NmServiceSpec constructNmServiceSpec(Identifier clientId, Identifier applicationId)
            throws InvalidApplicationIdException {
        final Application application = applications.findOne(Long.valueOf(applicationId.getValue()));
        if (application == null)
            throw new InvalidApplicationIdException("Application with id " + applicationId + " does not exist in repository");
        DockerContainerTemplate template = DockerContainerTemplate.copy(application.getDockerContainerTemplate());
        return new DockerContainerSpec(
                buildServiceName(application),
                template,
                Long.valueOf(clientId.getValue()));
    }

    @Loggable(LogLevel.DEBUG)
    public String buildServiceName(Application application) {
        return application.getName() + "-" + application.getId();
    }

    @Loggable(LogLevel.DEBUG)
    public DcnSpec constructDcnSpec(Identifier clientId, Identifier applicationId, NmServiceInfo serviceInfo) {
        DcnSpec dcn = new DcnSpec(buildDcnName(applicationId, clientId));
        if (serviceInfo != null && serviceInfo.getNetwork() != null)
            dcn.setNmServiceDeploymentNetworkDetails(serviceInfo.getNetwork());
        else
            log.warn("Failed to set NM service deployment network details in DCN spec");
        return dcn;
    }

    @Loggable(LogLevel.DEBUG)
    public String buildDcnName(Identifier applicationId, Identifier clientId) {
        return clientId + "-" + applicationId;
    }

}
