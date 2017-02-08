package net.geant.nmaas.nmservicedeployment.containerorchestrators.dockerengine.network;

import com.spotify.docker.client.messages.Ipam;
import com.spotify.docker.client.messages.IpamConfig;
import com.spotify.docker.client.messages.NetworkConfig;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.nmservicedeployment.containerorchestrators.dockerengine.DockerContainerSpec;
import net.geant.nmaas.nmservicedeployment.exceptions.ContainerNetworkDetailsVerificationException;
import net.geant.nmaas.nmservicedeployment.exceptions.NmServiceVerificationException;
import net.geant.nmaas.nmservicedeployment.nmservice.NmServiceDeploymentNetworkDetails;
import net.geant.nmaas.nmservicedeployment.nmservice.NmServiceInfo;
import net.geant.nmaas.nmservicedeployment.nmservice.NmServiceSpec;

import java.util.Arrays;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class ContainerNetworkConfigBuilder {

    private static final String DOCKER_NETWORK_DRIVER = "macvlan";
    private static final String DOCKER_MACVLAN_DRIVER_OPTION_MODE_KEY = "macvlan_mode";
    private static final String DOCKER_MACVLAN_DRIVER_OPTION_MORE_VALUE = "bridge";
    private static final String DOCKER_MACVLAN_DRIVER_OPTION_PARENT_KEY = "parent";

    public static NetworkConfig build(NmServiceInfo service)
            throws NmServiceVerificationException, ContainerNetworkDetailsVerificationException {
        verifyInputService(service);
        verifyInputNetworkDetails(service.getNetwork());
        final ContainerNetworkIpamSpec ipamSpec = ((ContainerNetworkDetails)service.getNetwork()).getIpAddresses();
        final IpamConfig ipamConfig = IpamConfig.create(
                ipamSpec.getSubnetWithMask(),
                ipamSpec.getIpRangeWithMask(),
                ipamSpec.getGateway());
        final Ipam ipam = Ipam.builder()
                .driver("default")
                .config(Arrays.asList(ipamConfig))
                .build();
        int vlanNumber = ((ContainerNetworkDetails)service.getNetwork()).getVlanNumber();
        String dataInterfaceName = ((DockerHost) service.getHost()).getDataInterfaceName();
        final NetworkConfig networkConfig = NetworkConfig.builder()
                .name(networkName(service.getSpec().uniqueDeploymentName(), vlanNumber))
                .driver(DOCKER_NETWORK_DRIVER)
                .checkDuplicate(true)
                .ipam(ipam)
                .addOption(DOCKER_MACVLAN_DRIVER_OPTION_MODE_KEY, DOCKER_MACVLAN_DRIVER_OPTION_MORE_VALUE)
                .addOption(DOCKER_MACVLAN_DRIVER_OPTION_PARENT_KEY, parentInterfaceName(dataInterfaceName, vlanNumber))
                .build();
        return networkConfig;
    }

    private static void verifyInputService(NmServiceInfo service) throws NmServiceVerificationException {
        if (service == null)
            throw new NmServiceVerificationException("Service object is null");
        NmServiceSpec spec = service.getSpec();
        if (spec == null)
            throw new NmServiceVerificationException("Service spec not available (null)");
        if (DockerContainerSpec.class != spec.getClass())
            throw new NmServiceVerificationException("Service spec not in DockerEngine format");
        if (spec.uniqueDeploymentName() == null || spec.uniqueDeploymentName().isEmpty())
            throw new NmServiceVerificationException("Service spec returns empty unique name for service deployment");
        if (service.getHost() == null)
            throw new NmServiceVerificationException("Deployment host not available (null)");
        if (DockerHost.class != service.getHost().getClass())
            throw new NmServiceVerificationException("Deployment host not in DockerEngine format");
        DockerHost host = (DockerHost) service.getHost();
        if (host.getDataInterfaceName() == null || host.getDataInterfaceName().isEmpty())
            throw new NmServiceVerificationException("Data interface name missing on Docker Host");
        if (service.getNetwork() == null)
            throw new NmServiceVerificationException("Deployment network details not available (null)");
    }

    private static void verifyInputNetworkDetails(NmServiceDeploymentNetworkDetails networkDetails) throws ContainerNetworkDetailsVerificationException {
        if (ContainerNetworkDetails.class != networkDetails.getClass())
            throw new ContainerNetworkDetailsVerificationException("Deployment network details not in DockerEngine format");
        ContainerNetworkDetails containerNetworkDetails = (ContainerNetworkDetails) networkDetails;
        if (containerNetworkDetails.getVlanNumber() == 0)
            throw new ContainerNetworkDetailsVerificationException("Assigned VLAN is missing");
        final ContainerNetworkIpamSpec ipamSpec = containerNetworkDetails.getIpAddresses();
        if (ipamSpec == null)
            throw new ContainerNetworkDetailsVerificationException("IPAM specification object is null");
        if (ipamSpec.getSubnetWithMask() == null || ipamSpec.getSubnetWithMask().isEmpty())
            throw new ContainerNetworkDetailsVerificationException("Subnet is missing");
        if (ipamSpec.getIpRangeWithMask() == null || ipamSpec.getIpRangeWithMask().isEmpty())
            throw new ContainerNetworkDetailsVerificationException("IP range is missing");
        if (ipamSpec.getGateway() == null || ipamSpec.getGateway().isEmpty())
            throw new ContainerNetworkDetailsVerificationException("Gateway is missing");
        if (!ipamSpec.getSubnetWithMask().equals(ipamSpec.getIpRangeWithMask()))
            throw new ContainerNetworkDetailsVerificationException("Subnet and IP range must be the same");
    }

    private static String networkName(String uniqueName, int vlanNumber) {
        return new StringBuilder().append(uniqueName).append("-network-vlan").append(vlanNumber).toString();
    }

    private static String parentInterfaceName(String interfaceName, int vlanNumber) {
        return new StringBuilder().append(interfaceName).append(".").append(vlanNumber).toString();
    }

}
