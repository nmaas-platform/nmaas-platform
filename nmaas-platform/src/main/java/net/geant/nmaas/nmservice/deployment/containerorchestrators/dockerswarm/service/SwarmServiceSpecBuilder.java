package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerswarm.service;

import com.spotify.docker.client.messages.swarm.ContainerSpec;
import com.spotify.docker.client.messages.swarm.ServiceSpec;
import com.spotify.docker.client.messages.swarm.TaskSpec;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerswarm.DockerSwarmNmServiceTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerswarm.DockerSwarmServiceSpec;
import net.geant.nmaas.nmservice.deployment.exceptions.NmServiceRequestVerificationException;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceSpec;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class SwarmServiceSpecBuilder {

    public static ServiceSpec build(NmServiceSpec spec) throws NmServiceRequestVerificationException {
        verifyInput(spec);
        DockerSwarmServiceSpec dockerSpec = (DockerSwarmServiceSpec) spec;
        DockerSwarmNmServiceTemplate dockerTemplate = dockerSpec.getTemplate();
        ServiceSpec.Builder serviceBuilder = ServiceSpec.builder();
        serviceBuilder.name(dockerSpec.getName());
        serviceBuilder.taskTemplate(
                TaskSpec.builder().containerSpec(
                        ContainerSpec.builder().image(dockerTemplate.getImage()).build()
                ).build()
        );
        return serviceBuilder.build();
    }

    public static void verifyInput(NmServiceSpec spec) throws NmServiceRequestVerificationException {
        if (DockerSwarmServiceSpec.class != spec.getClass())
            throw new NmServiceRequestVerificationException("Service spec not in Docker Swarm format");
        DockerSwarmServiceSpec dockerSwarmServiceSpec = (DockerSwarmServiceSpec) spec;
        if (DockerSwarmNmServiceTemplate.class != dockerSwarmServiceSpec.getTemplate().getClass())
            throw new NmServiceRequestVerificationException("Service template not in Docker Swarm format");
        DockerSwarmNmServiceTemplate template = dockerSwarmServiceSpec.getTemplate();
        if(!template.verify())
            throw new NmServiceRequestVerificationException("Service template incorrect");
        if(!template.verifyNmServiceSpec(spec))
            throw new NmServiceRequestVerificationException("Service spec incorrect or missing required data");
    }

}
