package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network;

import com.spotify.docker.client.messages.Ipam;
import com.spotify.docker.client.messages.IpamConfig;
import com.spotify.docker.client.messages.NetworkConfig;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerNetwork;
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

    public static NetworkConfig build(DockerNetwork dockerNetwork) throws DockerNetworkDetailsVerificationException {
        verifyInputDockerNetwork(dockerNetwork);
        final IpamConfig ipamConfig = IpamConfig.create(
                dockerNetwork.getSubnet(),
                dockerNetwork.getSubnet(),
                dockerNetwork.getGateway());
        final Ipam ipam = Ipam.builder()
                .driver("default")
                .config(Arrays.asList(ipamConfig))
                .build();
        int vlanNumber = dockerNetwork.getVlanNumber();
        String dataInterfaceName = dockerNetwork.getDockerHost().getDataInterfaceName();
        return NetworkConfig.builder()
                .name(networkName(dockerNetwork.getClientId().value(), vlanNumber))
                .driver(DOCKER_NETWORK_DRIVER)
                .checkDuplicate(true)
                .ipam(ipam)
                .addOption(DOCKER_MACVLAN_DRIVER_OPTION_MODE_KEY, DOCKER_MACVLAN_DRIVER_OPTION_MORE_VALUE)
                .addOption(DOCKER_MACVLAN_DRIVER_OPTION_PARENT_KEY, parentInterfaceName(dataInterfaceName, vlanNumber))
                .build();
    }

    private static void verifyInputDockerNetwork(DockerNetwork dockerNetwork) throws DockerNetworkDetailsVerificationException {
        if (dockerNetwork.getVlanNumber() == 0)
            throw new DockerNetworkDetailsVerificationException("VLAN is missing");
        if (dockerNetwork.getSubnet() == null || dockerNetwork.getSubnet().isEmpty())
            throw new DockerNetworkDetailsVerificationException("Subnet is missing");
        if (dockerNetwork.getGateway() == null || dockerNetwork.getGateway().isEmpty())
            throw new DockerNetworkDetailsVerificationException("Gateway is missing");
        if (dockerNetwork.getDockerHost() == null)
            throw new DockerNetworkDetailsVerificationException("Docker Host is missing");
        if (dockerNetwork.getDockerHost().getDataInterfaceName() == null || dockerNetwork.getDockerHost().getDataInterfaceName().isEmpty())
            throw new DockerNetworkDetailsVerificationException("Data interface name on Docker Host is missing");
    }

    private static String networkName(String uniqueName, int vlanNumber) {
        return new StringBuilder().append("nmaas-dcn-").append(uniqueName).append("-vlan").append(vlanNumber).toString();
    }

    private static String parentInterfaceName(String interfaceName, int vlanNumber) {
        return new StringBuilder().append(interfaceName).append(".").append(vlanNumber).toString();
    }

}
