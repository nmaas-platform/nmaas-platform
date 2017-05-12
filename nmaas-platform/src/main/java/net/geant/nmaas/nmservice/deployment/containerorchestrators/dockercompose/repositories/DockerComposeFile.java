package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.repositories;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerComposeFile {

    private byte[] configFileContent;

    public DockerComposeFile(byte[] configFileContent) {
        this.configFileContent = configFileContent;
    }

    public byte[] getConfigFileContent() {
        return configFileContent;
    }
}
