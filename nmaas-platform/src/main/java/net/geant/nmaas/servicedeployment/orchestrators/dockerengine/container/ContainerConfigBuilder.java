package net.geant.nmaas.servicedeployment.orchestrators.dockerengine.container;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.servicedeployment.exceptions.ServiceSpecVerificationException;
import net.geant.nmaas.servicedeployment.nmservice.NmServiceDeploymentHost;
import net.geant.nmaas.servicedeployment.nmservice.NmServiceSpec;
import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.DockerContainerSpec;
import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.DockerEngineContainerTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class ContainerConfigBuilder {

    public static ContainerConfig build(NmServiceSpec spec, NmServiceDeploymentHost host) {
        final DockerContainerSpec containerSpec = (DockerContainerSpec) spec;
        final DockerHost containerHost = (DockerHost) host;
        final ContainerConfigInput combinedSpec = ContainerConfigInput.fromSpec(containerSpec);
        final ContainerConfig.Builder containerBuilder = ContainerConfig.builder();
        containerBuilder.image(combinedSpec.getImage());
        if (combinedSpec.getCommand() != null)
            containerBuilder.cmd(combinedSpec.getCommand());
        containerBuilder.env(combinedSpec.getEnv());
        final HostConfig.Builder hostBuilder = HostConfig.builder();
        hostBuilder.portBindings(preparePortBindings(
                combinedSpec.getExposedPorts(),
                containerHost.getPublicIpAddress().toString(),
                containerHost.getAvailablePorts(combinedSpec.getExposedPorts().size())));
        final List<String> volumeBinds = prepareVolumeBindings(
                combinedSpec.getContainerVolumes(),
                containerHost.getVolumesPath(),
                containerSpec.uniqueDeploymentName());
        hostBuilder.appendBinds(volumeBinds);
        containerBuilder.hostConfig(HostConfig.builder().build());
        return containerBuilder.build();
    }

    private static Map<String, List<PortBinding>> preparePortBindings(List<ContainerPortForwardingSpec> containerPorts, String hostPublicIpAddress, List<Integer> availableHostPorts)  {
        final Map<String, List<PortBinding>> portBindings = new HashMap<>();
        int counter = 0;
        for (ContainerPortForwardingSpec port : containerPorts) {
            List<PortBinding> hostPorts = new ArrayList<>();
            hostPorts.add(PortBinding.of(hostPublicIpAddress, availableHostPorts.get(counter)));
            portBindings.put(port.getTargetPort() + "/" + port.getProtocol().getValue(), hostPorts); // TODO check if protocol is null ...
            counter++;
        }
        return portBindings;
    }

    private static List<String> prepareVolumeBindings(List<String> containerVolumes, String hostVolumesPath, String hostVolumeBaseName) {
        final List<String> volumeBinds = new ArrayList<>();
        int counter = 1;
        for (String containerVolume : containerVolumes) {
            volumeBinds.add(HostConfig.Bind
                    .from(generateNewHostVolume(hostVolumesPath, hostVolumeBaseName, counter))
                    .to(containerVolume).build().toString());
            counter++;
        }
        return volumeBinds;
    }

    private static String generateNewHostVolume(String hostVolumesPath, String hostVolumeName, int counter) {
        StringBuilder sb = new StringBuilder(hostVolumesPath);
        sb.append("/").append(hostVolumeName).append("-").append(counter);
        return sb.toString();
    }

    public static void verifyInput(NmServiceSpec spec) throws ServiceSpecVerificationException {
        if (DockerEngineContainerTemplate.class != spec.template().getClass() || DockerContainerSpec.class != spec.getClass())
            throw new ServiceSpecVerificationException("Service template and/or spec not in DockerEngine format");
        if(!spec.template().verify())
            throw new ServiceSpecVerificationException("Service template incorrect");
        if(!spec.template().verifyNmServiceSpec(spec))
            throw new ServiceSpecVerificationException("Service spec incorrect or missing required data");
    }

}
