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
}
