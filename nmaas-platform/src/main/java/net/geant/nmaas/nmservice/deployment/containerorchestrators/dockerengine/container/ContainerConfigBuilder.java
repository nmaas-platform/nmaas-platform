package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.container;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerContainerSpec;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerEngineContainerTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.ContainerNetworkDetails;
import net.geant.nmaas.nmservice.deployment.exceptions.NmServiceRequestVerificationException;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class ContainerConfigBuilder {

    public static ContainerConfig build(NmServiceInfo service) {
        final DockerHost host = (DockerHost) service.getHost();
        final ContainerNetworkDetails networkDetails = (ContainerNetworkDetails) service.getNetwork();
        final ContainerConfigInput configInput = ContainerConfigInput.fromSpec(service);
        final ContainerConfig.Builder containerBuilder = ContainerConfig.builder();
        containerBuilder.image(configInput.getImage());
        if (configInput.getCommand() != null)
            containerBuilder.cmd(configInput.getCommand());
        containerBuilder.env(configInput.getEnv());
        final HostConfig.Builder hostBuilder = HostConfig.builder();
        hostBuilder.portBindings(preparePortBindings(
                configInput.getExposedPort(),
                host.getPublicIpAddress().getHostAddress(),
                networkDetails.getPublicPort()));
        final List<String> volumeBinds = prepareVolumeBindings(
                configInput.getContainerVolumes(),
                host.getVolumesPath(),
                configInput.getUniqueDeploymentName());
        hostBuilder.appendBinds(volumeBinds);
        hostBuilder.privileged(true);
        final HostConfig hostConfig = hostBuilder.build();
        containerBuilder.hostConfig(hostConfig);
        return containerBuilder.build();
    }

    private static Map<String, List<PortBinding>> preparePortBindings(ContainerPortForwardingSpec containerPort, String hostPublicIpAddress, int assignedHostPort)  {
        final Map<String, List<PortBinding>> portBindings = new HashMap<>();
        List<PortBinding> hostPorts = new ArrayList<>();
        hostPorts.add(PortBinding.of(hostPublicIpAddress, assignedHostPort));
        portBindings.put(exposedPortString(containerPort), hostPorts);
        return portBindings;
    }

    private static String exposedPortString(ContainerPortForwardingSpec port) {
        StringBuilder sb = new StringBuilder();
        sb.append(port.getTargetPort());
        if (port.getProtocol() != null)
            sb.append("/").append(port.getProtocol().getValue());
        return sb.toString();
    }

    private static List<String> prepareVolumeBindings(List<String> containerVolumes, String hostVolumesPath, String hostVolumeBaseName) {
        final List<String> volumeBinds = new ArrayList<>();
        int counter = 1;
        for (String containerVolume : containerVolumes) {
            volumeBinds.add(HostConfig.Bind
                    .from(generateNewHostVolume(hostVolumesPath, hostVolumeBaseName, counter))
                    .to(containerVolume)
                    .build().toString());
            counter++;
        }
        return volumeBinds;
    }

    private static String generateNewHostVolume(String hostVolumesPath, String hostVolumeName, int counter) {
        StringBuilder sb = new StringBuilder(hostVolumesPath);
        sb.append("/").append(generateNewHostVolumeDirectoryName(hostVolumeName, counter));
        return sb.toString();
    }

    private static String generateNewHostVolumeDirectoryName(String hostVolumeName, int counter) {
        return hostVolumeName + "-" + counter;
    }

    public static void verifyInput(NmServiceInfo service) throws NmServiceRequestVerificationException {
        NmServiceSpec spec = service.getSpec();
        if (DockerEngineContainerTemplate.class != spec.template().getClass() || DockerContainerSpec.class != spec.getClass())
            throw new NmServiceRequestVerificationException("Service template and/or spec not in DockerEngine format");
        if (!spec.template().verify())
            throw new NmServiceRequestVerificationException("Service template incorrect");
        if (!spec.template().verifyNmServiceSpec(spec))
            throw new NmServiceRequestVerificationException("Service spec incorrect or missing required data");
        if (service.getNetwork() == null)
            throw new NmServiceRequestVerificationException("Network details not set");
        if (ContainerNetworkDetails.class != service.getNetwork().getClass())
            throw new NmServiceRequestVerificationException("Network details not in DockerEngine format");
        ContainerNetworkDetails networkDetails = (ContainerNetworkDetails) service.getNetwork();
        if (networkDetails.getPublicPort() == 0
                || networkDetails.getVlanNumber() == 0
                || networkDetails.getIpAddresses() == null
                || !networkDetails.getIpAddresses().verify()) {
            throw new NmServiceRequestVerificationException("Network details not valid. Some parameters are be missing.");
        }
    }

    public static String getPrimaryVolumeName(String baseName) {
        return generateNewHostVolumeDirectoryName(baseName, 1);
    }
}
