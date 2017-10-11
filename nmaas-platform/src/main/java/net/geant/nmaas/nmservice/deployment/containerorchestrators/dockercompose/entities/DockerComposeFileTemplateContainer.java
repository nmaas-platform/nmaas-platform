package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities;

public class DockerComposeFileTemplateContainer {

    private String containerName;

    private String containerIpAddress;

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getContainerIpAddress() {
        return containerIpAddress;
    }

    public void setContainerIpAddress(String containerIpAddress) {
        this.containerIpAddress = containerIpAddress;
    }
}
