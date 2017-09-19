package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import net.geant.nmaas.externalservices.inventory.network.BasicCustomerNetworkAttachPoint;
import net.geant.nmaas.externalservices.inventory.network.repositories.BasicCustomerNetworkAttachPointRepository;
import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeServiceComponent;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.DockerNetworkResourceManager;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerOrchestratorInternalErrorException;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import net.geant.nmaas.utils.ssh.CommandExecutionException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class StaticRoutingConfigManager {

    private final static Logger log = LogManager.getLogger(StaticRoutingConfigManager.class);

    @Autowired
    private BasicCustomerNetworkAttachPointRepository customerNetworks;
    @Autowired
    private DockerNetworkResourceManager dockerNetworkResourceManager;
    @Autowired
    private NmServiceRepositoryManager nmServiceRepositoryManager;

    @Autowired
    private DockerComposeCommandExecutor composeCommandExecutor;

    @Loggable(LogLevel.INFO)
    @Transactional
    public void configure(Identifier deploymentId) throws ContainerOrchestratorInternalErrorException, CommandExecutionException, InvalidDeploymentIdException {
        NmServiceInfo service = nmServiceRepositoryManager.loadService(deploymentId);
        BasicCustomerNetworkAttachPoint customerNetwork = customerNetworks.findByCustomerId(service.getClientId().longValue())
                .orElseThrow(() -> new ContainerOrchestratorInternalErrorException("No network details information found for customer with id " + service.getClientId()));
        List<String> networks = obtainListOfCustomerNetworks(customerNetwork);
        List<String> devices = obtainListOfCustomerDevices(customerNetwork);
        networks.addAll(devices.stream().map(d -> d + "/32").collect(Collectors.toList()));
        log.debug("Setting routing entries for " + networks.size() + " networks");
        for (DockerComposeServiceComponent component : service.getDockerComposeService().getServiceComponents()) {
            log.debug("Executing commands for component " + component.getName());
            addRoutesForEachCustomerNetworkAddress(service, networks, component);
        }
    }

    private List<String> obtainListOfCustomerNetworks(BasicCustomerNetworkAttachPoint customerNetwork) {
        return new ArrayList<>(customerNetwork.getMonitoredEquipment().getNetworks());
    }

    private List<String> obtainListOfCustomerDevices(BasicCustomerNetworkAttachPoint customerNetwork) {
        return new ArrayList<>(customerNetwork.getMonitoredEquipment().getAddresses());
    }

    private void addRoutesForEachCustomerNetworkAddress(NmServiceInfo service, List<String> networks, DockerComposeServiceComponent component) throws CommandExecutionException, ContainerOrchestratorInternalErrorException {
        for (String network : networks) {
            addStaticRouteOnContainer(
                    service.getDeploymentId(),
                    component.getDeploymentName(),
                    service.getHost(),
                    addIpRouteCommand(network, dockerNetworkResourceManager.obtainGatewayFromClientNetwork(service.getClientId())));
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
