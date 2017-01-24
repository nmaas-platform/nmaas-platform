package net.geant.nmaas.servicedeployment.orchestrators.dockerengine.container;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import net.geant.nmaas.servicedeployment.exceptions.ServiceSpecVerificationException;
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

    public static ContainerConfig build(NmServiceSpec spec) {
        DockerEngineContainerTemplate dockerTemplate = (DockerEngineContainerTemplate) spec.template();
        DockerContainerSpec dockerSpec = (DockerContainerSpec) spec;
        ContainerConfig.Builder containerBuilder = ContainerConfig.builder();
        containerBuilder.image(dockerTemplate.getImage());
        containerBuilder.cmd(dockerTemplate.getCommand());
        containerBuilder.env(dockerTemplate.getEnv());

        final Map<String, List<PortBinding>> portBindings = new HashMap<>();
        for (PortForwardingSpec port : dockerTemplate.getPorts()) {
            List<PortBinding> hostPorts = new ArrayList<>();
            hostPorts.add(PortBinding.of("0.0.0.0", port.getPublishedPort()));
            portBindings.put(port.getTargetPort() + "/" + port.getProtocol().name(), hostPorts);
        }

        containerBuilder.hostConfig(HostConfig.builder().portBindings(portBindings).build());
        return containerBuilder.build();
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
