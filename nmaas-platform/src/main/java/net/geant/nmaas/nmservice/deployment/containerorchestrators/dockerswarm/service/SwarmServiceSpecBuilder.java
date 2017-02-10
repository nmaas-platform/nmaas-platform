package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerswarm.service;

import com.spotify.docker.client.messages.swarm.ContainerSpec;
import com.spotify.docker.client.messages.swarm.ServiceSpec;
import com.spotify.docker.client.messages.swarm.TaskSpec;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerswarm.DockerSwarmNmServiceTemplate;
import net.geant.nmaas.nmservice.deployment.exceptions.NmServiceVerificationException;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerswarm.DockerSwarmServiceSpec;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceSpec;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class SwarmServiceSpecBuilder {

    public static ServiceSpec build(NmServiceSpec spec) throws NmServiceVerificationException {
        verifyInput(spec.template(), spec);
        DockerSwarmNmServiceTemplate dockerTemplate = (DockerSwarmNmServiceTemplate) spec.template();
        DockerSwarmServiceSpec dockerSpec = (DockerSwarmServiceSpec) spec;
        ServiceSpec.Builder serviceBuilder = ServiceSpec.builder();
        serviceBuilder.withName(dockerSpec.name());
        serviceBuilder.withTaskTemplate(
                TaskSpec.builder().withContainerSpec(
                        ContainerSpec.builder().withImage(dockerTemplate.getImage()).build()
                ).build()
        );
        return serviceBuilder.build();
    }

    public static void verifyInput(NmServiceTemplate template, NmServiceSpec spec) throws NmServiceVerificationException {
        if (DockerSwarmNmServiceTemplate.class != template.getClass() || DockerSwarmServiceSpec.class != spec.getClass())
            throw new NmServiceVerificationException("Service template and/or spec not in DockerSwarm format");
        if(!template.verify())
            throw new NmServiceVerificationException("Service template incorrect");
        if(!template.verifyNmServiceSpec(spec))
            throw new NmServiceVerificationException("Service spec incorrect or missing required data");
    }

}
