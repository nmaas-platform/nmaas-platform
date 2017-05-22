package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.repositories;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerComposeFile {

    public static final String DEFAULT_DOCKER_COMPOSE_FILE_NAME = "docker-compose.yml";

    private byte[] composeFileContent;

    public DockerComposeFile(byte[] configFileContent) {
        this.composeFileContent = configFileContent;
    }

    public byte[] getComposeFileContent() {
        return composeFileContent;
    }

    public enum TemplateVariable {
        CONTAINER_NAME("container_name"),
        PORT("port"),
        VOLUME("volume"),
        CONTAINER_IP_ADDRESS("container_ip_address"),
        ACCESS_DOCKER_NETWORK_NAME("nmaas_ext_access_network"),
        DCN_DOCKER_NETWORK_NAME("nmaas_dcn_network");

        private String value;

        TemplateVariable(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }
}
