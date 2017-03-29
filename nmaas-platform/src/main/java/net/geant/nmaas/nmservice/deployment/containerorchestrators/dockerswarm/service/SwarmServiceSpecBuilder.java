package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerswarm.service;

import com.spotify.docker.client.messages.swarm.ContainerSpec;
import com.spotify.docker.client.messages.swarm.ServiceSpec;
import com.spotify.docker.client.messages.swarm.TaskSpec;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerswarm.DockerSwarmNmServiceTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerswarm.DockerSwarmServiceSpec;
import net.geant.nmaas.nmservice.deployment.exceptions.NmServiceRequestVerificationException;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceSpec;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceTemplate;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class SwarmServiceSpecBuilder {

    public static ServiceSpec build(NmServiceSpec spec) throws NmServiceRequestVerificationException {
        verifyInput(spec.template(), spec);
        DockerSwarmNmServiceTemplate dockerTemplate = (DockerSwarmNmServiceTemplate) spec.template();
        DockerSwarmServiceSpec dockerSpec = (DockerSwarmServiceSpec) spec;
        ServiceSpec.Builder serviceBuilder = ServiceSpec.builder();
        serviceBuilder.name(dockerSpec.name());
        serviceBuilder.taskTemplate(
                TaskSpec.builder().containerSpec(
                        ContainerSpec.builder().image(dockerTemplate.getImage()).build()
                ).build()
        );
        return serviceBuilder.build();
    }

    public static void verifyInput(NmServiceTemplate template, NmServiceSpec spec) throws NmServiceRequestVerificationException {
        if (DockerSwarmNmServiceTemplate.class != template.getClass() || DockerSwarmServiceSpec.class != spec.getClass())
            throw new NmServiceRequestVerificationException("Service template and/or spec not in DockerSwarm format");
        if(!template.verify())
            throw new NmServiceRequestVerificationException("Service template incorrect");
        if(!template.verifyNmServiceSpec(spec))
            throw new NmServiceRequestVerificationException("Service spec incorrect or missing required data");
    }

}
