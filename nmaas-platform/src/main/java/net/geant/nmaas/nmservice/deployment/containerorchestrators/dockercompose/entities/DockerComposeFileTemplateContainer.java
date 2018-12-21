package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DockerComposeFileTemplateContainer {

    private String containerName;

    private String containerIpAddress;
}
