package net.geant.nmaas.servicedeployment.orchestrators.dockerswarm.service;

import com.spotify.docker.client.messages.swarm.ContainerSpec;
import com.spotify.docker.client.messages.swarm.ServiceSpec;
import com.spotify.docker.client.messages.swarm.TaskSpec;
import net.geant.nmaas.servicedeployment.exceptions.ServiceSpecVerificationException;
import net.geant.nmaas.servicedeployment.nmservice.NmServiceSpec;
import net.geant.nmaas.servicedeployment.nmservice.NmServiceTemplate;
import net.geant.nmaas.servicedeployment.orchestrators.dockerswarm.DockerSwarmNmServiceTemplate;
import net.geant.nmaas.servicedeployment.orchestrators.dockerswarm.DockerSwarmServiceSpec;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class ServiceSpecBuilder {

    public static ServiceSpec build(NmServiceSpec spec)
            throws ServiceSpecVerificationException {
        verifyInput(spec.template(), spec);
        DockerSwarmNmServiceTemplate dockerTemplate = (DockerSwarmNmServiceTemplate) spec.template();
        DockerSwarmServiceSpec dockerSpec = (DockerSwarmServiceSpec) spec;
        ServiceSpec.Builder serviceBuilder = new ServiceSpec.Builder();
        serviceBuilder.withName(dockerSpec.name());
        serviceBuilder.withTaskTemplate(
                new TaskSpec.Builder().withContainerSpec(
                        new ContainerSpec.Builder().withImage(dockerTemplate.getImage()).build()
                ).build()
        );
        return serviceBuilder.build();
    }

    public static void verifyInput(NmServiceTemplate template, NmServiceSpec spec) throws ServiceSpecVerificationException {
        if (DockerSwarmNmServiceTemplate.class != template.getClass()
                || DockerSwarmServiceSpec.class != spec.getClass())
            throw new ServiceSpecVerificationException("Service template and/or spec not in DockerSwarm format");
        if(!template.verify())
            throw new ServiceSpecVerificationException("Service template incorrect");
        if(!template.verifyNmServiceSpec(spec))
            throw new ServiceSpecVerificationException("Service spec incorrect or missing required data");
    }

}
