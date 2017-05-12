package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.repositories;

import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfiguration;
import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfigurationRepository;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class DockerComposeFileRepository {

    private Map<Identifier, DockerComposeFile> files = new HashMap<>();

    public void storeFileContent(Identifier deploymentId, DockerComposeFile composeFile) {
        files.put(deploymentId, composeFile);
    }

    public class DockerComposeFileNotFoundException extends Exception {

        public DockerComposeFileNotFoundException(String message) {
            super(message);
        }

    }

}
