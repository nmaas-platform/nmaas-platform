package net.geant.nmaas.orchestration.tasks;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.nmservice.DeploymentIdToNmServiceNameMapper;
import net.geant.nmaas.nmservice.configuration.NmServiceConfigurationProvider;
import net.geant.nmaas.nmservice.configuration.exceptions.NmServiceConfigurationFailedException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.container.ContainerDeploymentDetails;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.repository.NmServiceRepository;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.AppApplyConfigurationActionEvent;
import net.geant.nmaas.orchestration.events.AppDeploymentErrorEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AppConfigurationTask {

    private final static Logger log = LogManager.getLogger(AppConfigurationTask.class);

    private NmServiceConfigurationProvider serviceConfiguration;

    private DeploymentIdToNmServiceNameMapper deploymentIdToNmServiceNameMapper;

    private NmServiceRepository nmServiceRepository;

    private AppDeploymentRepository repository;

    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public AppConfigurationTask(
            NmServiceConfigurationProvider serviceConfiguration,
            DeploymentIdToNmServiceNameMapper deploymentIdToNmServiceNameMapper,
            NmServiceRepository nmServiceRepository,
            AppDeploymentRepository repository,
            ApplicationEventPublisher applicationEventPublisher) {
        this.serviceConfiguration = serviceConfiguration;
        this.deploymentIdToNmServiceNameMapper = deploymentIdToNmServiceNameMapper;
        this.nmServiceRepository = nmServiceRepository;
        this.repository = repository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Async
    @EventListener
    public void applyConfiguration(AppApplyConfigurationActionEvent event) throws InvalidDeploymentIdException {
        final Identifier deploymentId = event.getDeploymentId();
        AppDeployment appDeployment = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        DockerHost dockerHost = null;
        ContainerDeploymentDetails containerDetails = null;
        try {
            String serviceName = deploymentIdToNmServiceNameMapper.nmServiceName(deploymentId);
            NmServiceInfo serviceInfo = nmServiceRepository.loadService(serviceName);
            dockerHost = (DockerHost) serviceInfo.getHost();
            containerDetails = (ContainerDeploymentDetails) serviceInfo.getDetails();
        } catch (DeploymentIdToNmServiceNameMapper.EntryNotFoundException
                | NmServiceRepository.ServiceNotFoundException e) {
            log.error("Exception during application configuration preparation -> " + e.getMessage());
            applicationEventPublisher.publishEvent(new AppDeploymentErrorEvent(this, deploymentId));
            return;
        }
        try {
            serviceConfiguration.configureNmService(deploymentId, appDeployment.getApplicationId(), appDeployment.getConfiguration(), dockerHost, containerDetails);
        } catch (NmServiceConfigurationFailedException e) {
            log.warn("Service configuration failed for deployment " + deploymentId.value() + " -> " + e.getMessage());
        }
    }

}
