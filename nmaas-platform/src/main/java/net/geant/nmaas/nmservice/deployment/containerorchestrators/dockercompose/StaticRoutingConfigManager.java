package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import net.geant.nmaas.externalservices.inventory.network.DomainNetworkAttachPoint;
import net.geant.nmaas.externalservices.inventory.network.repositories.DomainNetworkAttachPointRepository;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeServiceComponent;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.network.DockerNetworkResourceManager;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerOrchestratorInternalErrorException;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import net.geant.nmaas.utils.ssh.CommandExecutionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Profile("env_docker-compose")
class StaticRoutingConfigManager {

    private final static Logger log = LogManager.getLogger(StaticRoutingConfigManager.class);

    private DomainNetworkAttachPointRepository customerNetworks;
    private DockerNetworkResourceManager dockerNetworkResourceManager;
    private DockerComposeServiceRepositoryManager nmServiceRepositoryManager;
    private DockerComposeCommandExecutor composeCommandExecutor;

    @Autowired
    public StaticRoutingConfigManager(DomainNetworkAttachPointRepository customerNetworks, DockerNetworkResourceManager dockerNetworkResourceManager, DockerComposeServiceRepositoryManager nmServiceRepositoryManager, DockerComposeCommandExecutor composeCommandExecutor) {
        this.customerNetworks = customerNetworks;
        this.dockerNetworkResourceManager = dockerNetworkResourceManager;
        this.nmServiceRepositoryManager = nmServiceRepositoryManager;
        this.composeCommandExecutor = composeCommandExecutor;
    }

    @Loggable(LogLevel.INFO)
    @Transactional
    void configure(Identifier deploymentId) throws ContainerOrchestratorInternalErrorException, CommandExecutionException, InvalidDeploymentIdException {
        DockerComposeNmServiceInfo service = nmServiceRepositoryManager.loadService(deploymentId);
        DomainNetworkAttachPoint customerNetwork = customerNetworks.findByDomain(service.getDomain())
                .orElseThrow(() -> new ContainerOrchestratorInternalErrorException("No network details information found for domain " + service.getDomain()));
        List<String> networks = obtainListOfCustomerNetworks(customerNetwork);
        List<String> devices = obtainListOfCustomerDevices(customerNetwork);
        networks.addAll(devices.stream().map(d -> d + "/32").collect(Collectors.toList()));
        List<DockerComposeServiceComponent> components = service.getDockerComposeService().getServiceComponents();
        log.debug("Setting routing entries for " + components.size() + " components and " + networks.size() + " networks");
        for (DockerComposeServiceComponent component : components) {
            log.debug("Executing commands for component " + component.getName());
            addRoutesForEachCustomerNetworkAddress(service, networks, component);
        }
    }

    private List<String> obtainListOfCustomerNetworks(DomainNetworkAttachPoint customerNetwork) {
        return new ArrayList<>(customerNetwork.getMonitoredEquipment().getNetworks());
    }

    private List<String> obtainListOfCustomerDevices(DomainNetworkAttachPoint customerNetwork) {
        return new ArrayList<>(customerNetwork.getMonitoredEquipment().getAddresses());
    }

    private void addRoutesForEachCustomerNetworkAddress(DockerComposeNmServiceInfo service, List<String> networks, DockerComposeServiceComponent component) throws CommandExecutionException, ContainerOrchestratorInternalErrorException {
        for (String network : networks) {
            addStaticRouteOnContainer(
                    service.getDeploymentId(),
                    component.getDeploymentName(),
                    service.getHost(),
                    addIpRouteCommand(network, dockerNetworkResourceManager.obtainGatewayFromClientNetwork(service.getDomain())));
        }
    }

    private void addStaticRouteOnContainer(Identifier deploymentId, String containerDeploymentName, DockerHost dockerHost, String command) throws CommandExecutionException {
        composeCommandExecutor.executeComposeExecCommand(deploymentId, dockerHost, commandBodyWithPrecedingContainerName(containerDeploymentName, command));
    }

    private String commandBodyWithPrecedingContainerName(String containerDeploymentName, String command) {
        return containerDeploymentName + " " + command;
    }

    private String addIpRouteCommand(String networkAddress, String gatewayAddress) {
        StringBuilder command = new StringBuilder();
        command.append("ip").append(" ")
                .append("route").append(" ")
                .append("add").append(" ")
                .append(networkAddress).append(" ")
                .append("via").append(" ")
                .append(gatewayAddress);
        return command.toString();
    }

}
