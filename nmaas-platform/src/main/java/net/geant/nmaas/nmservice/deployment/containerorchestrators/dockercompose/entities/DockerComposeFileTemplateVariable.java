package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities;

public enum DockerComposeFileTemplateVariable {

    CONTAINER_NAME("container_name"),
    PORT("port"),
    VOLUME("volume"),
    CONTAINER_IP_ADDRESS("container_ip_address"),
    ACCESS_DOCKER_NETWORK_NAME("nmaas_ext_access_network"),
    DCN_DOCKER_NETWORK_NAME("nmaas_dcn_network");

    private String value;

    DockerComposeFileTemplateVariable(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}
