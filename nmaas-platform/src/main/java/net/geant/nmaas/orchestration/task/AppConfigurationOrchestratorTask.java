package net.geant.nmaas.orchestration.task;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.nmservice.DeploymentIdToNmServiceNameMapper;
import net.geant.nmaas.nmservice.configuration.NmServiceConfigurationProvider;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.container.ContainerDeploymentDetails;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.repository.NmServiceRepository;
import net.geant.nmaas.orchestration.AppConfiguration;
import net.geant.nmaas.orchestration.AppDeploymentErrorEvent;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AppConfigurationOrchestratorTask {

    private final static Logger log = LogManager.getLogger(AppConfigurationOrchestratorTask.class);

    private NmServiceConfigurationProvider serviceConfiguration;

    private DeploymentIdToNmServiceNameMapper deploymentIdToNmServiceNameMapper;

    private NmServiceRepository nmServiceRepository;

    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public AppConfigurationOrchestratorTask(
            NmServiceConfigurationProvider serviceConfiguration,
            DeploymentIdToNmServiceNameMapper deploymentIdToNmServiceNameMapper,
            NmServiceRepository nmServiceRepository,
            ApplicationEventPublisher applicationEventPublisher) {
        this.serviceConfiguration = serviceConfiguration;
        this.deploymentIdToNmServiceNameMapper = deploymentIdToNmServiceNameMapper;
        this.nmServiceRepository = nmServiceRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Loggable(LogLevel.INFO)
    public void configure(Identifier deploymentId, AppConfiguration configuration) {
        verifyIfAllPropertiesAreSet(deploymentId, configuration);
        try {
            String serviceName = deploymentIdToNmServiceNameMapper.nmServiceName(deploymentId);
            NmServiceInfo serviceInfo = nmServiceRepository.loadService(serviceName);
            DockerHost dockerHost = (DockerHost) serviceInfo.getHost();
            ContainerDeploymentDetails containerDetails = (ContainerDeploymentDetails) serviceInfo.getDetails();
            serviceConfiguration.configureNmService(deploymentId, configuration, dockerHost, containerDetails);
        } catch (DeploymentIdToNmServiceNameMapper.EntryNotFoundException
                | NmServiceRepository.ServiceNotFoundException e) {
            log.error("Exception during application configuration -> " + e.getMessage());
            applicationEventPublisher.publishEvent(new AppDeploymentErrorEvent(this, deploymentId));
        }
    }

    private void verifyIfAllPropertiesAreSet(Identifier deploymentId, AppConfiguration configuration) {
        if (deploymentId == null || configuration == null)
            throw new NullPointerException();
    }

}
