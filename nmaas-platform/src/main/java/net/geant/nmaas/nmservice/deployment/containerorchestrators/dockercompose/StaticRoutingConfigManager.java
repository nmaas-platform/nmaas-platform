package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import net.geant.nmaas.externalservices.inventory.network.BasicCustomerNetworkAttachPoint;
import net.geant.nmaas.externalservices.inventory.network.repositories.BasicCustomerNetworkAttachPointRepository;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerOrchestratorInternalErrorException;
import net.geant.nmaas.utils.ssh.CommandExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class StaticRoutingConfigManager {

    @Autowired
    private BasicCustomerNetworkAttachPointRepository customerNetworks;

    @Autowired
    private DockerComposeCommandExecutor composeCommandExecutor;

    @Transactional
    public void configure(NmServiceInfo service) throws ContainerOrchestratorInternalErrorException, CommandExecutionException {
        BasicCustomerNetworkAttachPoint customerNetwork = customerNetworks.findByCustomerId(service.getClientId().longValue())
                .orElseThrow(() -> new ContainerOrchestratorInternalErrorException("No network details information found for customer with id " + service.getClientId()));
        List<String> networks = obtainListOfCustomerNetworks(customerNetwork);
        List<String> devices = obtainListOfCustomerDevices(customerNetwork, service.getManagedDevicesIpAddresses());
        networks.addAll(devices.stream().map(d -> d + "/32").collect(Collectors.toList()));
        for (String network : networks) {
            addStaticRouteOnContainer(
                    service,
                    addIpRouteCommand(network, service.getDockerContainer().getNetworkDetails().getIpAddresses().getGateway()));
        }
    }

    private List<String> obtainListOfCustomerDevices(BasicCustomerNetworkAttachPoint customerNetwork, List<String> monitoredDevices) {
        List<String> devices = customerNetwork.getMonitoredEquipment().getAddresses();
        if (monitoredDevices != null)
            monitoredDevices.stream().filter(d -> !devices.contains(d)).forEach(d -> devices.add(d));
        return devices;
    }

    private List<String> obtainListOfCustomerNetworks(BasicCustomerNetworkAttachPoint customerNetwork) {
        return customerNetwork.getMonitoredEquipment().getNetworks();
    }

    private void addStaticRouteOnContainer(NmServiceInfo service, String command) throws CommandExecutionException {
        composeCommandExecutor.executeComposeExecCommand(service.getDeploymentId(), service.getHost(), commandBodyWithPrecedingContainerName(service, command));
    }

    private String commandBodyWithPrecedingContainerName(NmServiceInfo service, String command) {
        return service.getDeploymentId() + " " + command;
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
