package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network;

import com.spotify.docker.client.messages.Ipam;
import com.spotify.docker.client.messages.IpamConfig;
import com.spotify.docker.client.messages.NetworkConfig;
import net.geant.nmaas.nmservice.deployment.entities.DockerHostNetwork;
import net.geant.nmaas.nmservice.deployment.exceptions.DockerNetworkDetailsVerificationException;

import java.util.Arrays;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerNetworkConfigBuilder {

    private static final String DOCKER_NETWORK_DRIVER = "macvlan";
    private static final String DOCKER_MACVLAN_DRIVER_OPTION_MODE_KEY = "macvlan_mode";
    private static final String DOCKER_MACVLAN_DRIVER_OPTION_MORE_VALUE = "bridge";
    private static final String DOCKER_MACVLAN_DRIVER_OPTION_PARENT_KEY = "parent";

    public static NetworkConfig build(DockerHostNetwork dockerHostNetwork) throws DockerNetworkDetailsVerificationException {
        verifyInputDockerNetwork(dockerHostNetwork);
        final IpamConfig ipamConfig = IpamConfig.create(
                dockerHostNetwork.getSubnet(),
                dockerHostNetwork.getSubnet(),
                dockerHostNetwork.getGateway());
        final Ipam ipam = Ipam.builder()
                .driver("default")
                .config(Arrays.asList(ipamConfig))
                .build();
        int vlanNumber = dockerHostNetwork.getVlanNumber();
        String dataInterfaceName = dockerHostNetwork.getHost().getDataInterfaceName();
        return NetworkConfig.builder()
                .name(networkName(dockerHostNetwork.getClientId().value(), vlanNumber))
                .driver(DOCKER_NETWORK_DRIVER)
                .checkDuplicate(true)
                .ipam(ipam)
                .addOption(DOCKER_MACVLAN_DRIVER_OPTION_MODE_KEY, DOCKER_MACVLAN_DRIVER_OPTION_MORE_VALUE)
                .addOption(DOCKER_MACVLAN_DRIVER_OPTION_PARENT_KEY, parentInterfaceName(dataInterfaceName, vlanNumber))
                .build();
    }

    private static void verifyInputDockerNetwork(DockerHostNetwork dockerHostNetwork) throws DockerNetworkDetailsVerificationException {
        if (dockerHostNetwork.getVlanNumber() == 0)
            throw new DockerNetworkDetailsVerificationException("VLAN is missing");
        if (dockerHostNetwork.getSubnet() == null || dockerHostNetwork.getSubnet().isEmpty())
            throw new DockerNetworkDetailsVerificationException("Subnet is missing");
        if (dockerHostNetwork.getGateway() == null || dockerHostNetwork.getGateway().isEmpty())
            throw new DockerNetworkDetailsVerificationException("Gateway is missing");
        if (dockerHostNetwork.getHost() == null)
            throw new DockerNetworkDetailsVerificationException("Docker Host is missing");
        if (dockerHostNetwork.getHost().getDataInterfaceName() == null || dockerHostNetwork.getHost().getDataInterfaceName().isEmpty())
            throw new DockerNetworkDetailsVerificationException("Data interface name on Docker Host is missing");
    }

    private static String networkName(String uniqueName, int vlanNumber) {
        return new StringBuilder().append("nmaas-dcn-").append(uniqueName).append("-vlan").append(vlanNumber).toString();
    }

    private static String parentInterfaceName(String interfaceName, int vlanNumber) {
        return new StringBuilder().append(interfaceName).append(".").append(vlanNumber).toString();
    }

}
