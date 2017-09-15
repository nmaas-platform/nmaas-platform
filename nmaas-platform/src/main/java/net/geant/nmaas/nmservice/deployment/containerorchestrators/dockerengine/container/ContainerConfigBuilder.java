package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.container;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerNetDetails;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerPortForwarding;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.exceptions.NmServiceRequestVerificationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class ContainerConfigBuilder {

    public static ContainerConfig build(NmServiceInfo service) {
        final DockerContainerNetDetails networkDetails = service.getDockerContainer().getNetworkDetails();
        final ContainerConfigInput configInput = ContainerConfigInput.fromSpec(service);
        final ContainerConfig.Builder containerBuilder = ContainerConfig.builder();
        containerBuilder.image(configInput.getImage());
        if (configInput.getCommand() != null)
            containerBuilder.cmd(configInput.getCommand());
        containerBuilder.env(configInput.getEnv());
        final HostConfig.Builder hostBuilder = HostConfig.builder();
        hostBuilder.portBindings(preparePortBindings(
                configInput.getExposedPort(),
                service.getHost().getPublicIpAddress().getHostAddress(),
                networkDetails.getPublicPort()));
        final List<String> volumeBinds = prepareVolumeBindings(
                configInput.getContainerVolumes(),
                service.getHost().getVolumesPath(),
                configInput.getUniqueDeploymentName());
        hostBuilder.appendBinds(volumeBinds);
        hostBuilder.privileged(true);
        final HostConfig hostConfig = hostBuilder.build();
        containerBuilder.hostConfig(hostConfig);
        return containerBuilder.build();
    }

    private static Map<String, List<PortBinding>> preparePortBindings(DockerContainerPortForwarding containerPort, String hostPublicIpAddress, int assignedHostPort)  {
        final Map<String, List<PortBinding>> portBindings = new HashMap<>();
        List<PortBinding> hostPorts = new ArrayList<>();
        hostPorts.add(PortBinding.of(hostPublicIpAddress, assignedHostPort));
        portBindings.put(exposedPortString(containerPort), hostPorts);
        return portBindings;
    }

    private static String exposedPortString(DockerContainerPortForwarding port) {
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

    public static void verifyInitInput(NmServiceInfo service) throws NmServiceRequestVerificationException {
        if (service.getTemplate() == null)
            throw new NmServiceRequestVerificationException("Service template not set");
        if (service.getHost() == null)
            throw new NmServiceRequestVerificationException("Docker host not set");
        if (service.getDockerContainer() == null)
            throw new NmServiceRequestVerificationException("Docker container initial details are missing");
    }

    public static void verifyFinalInput(NmServiceInfo service) throws NmServiceRequestVerificationException {
        verifyInitInput(service);
        if (service.getDockerContainer().getNetworkDetails() == null)
            throw new NmServiceRequestVerificationException("Docker container network details not set");
        DockerContainerNetDetails networkDetails = service.getDockerContainer().getNetworkDetails();
        if (networkDetails.getPublicPort() == 0
                || networkDetails.getIpam() == null
                || !networkDetails.getIpam().verify()) {
            throw new NmServiceRequestVerificationException("Network details not valid. Some parameters are be missing.");
        }
    }

    static String getPrimaryVolumeName(String baseName) {
        return generateNewHostVolumeDirectoryName(baseName, 1);
    }
}
