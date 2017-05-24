package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.repositories;

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

    public DockerComposeFile loadFileContent(Identifier deploymentId) throws DockerComposeFileNotFoundException {
        DockerComposeFile file = files.get(deploymentId);
        if (file != null)
            return file;
        else
            throw new DockerComposeFileNotFoundException("Docker compose file for deployment " + deploymentId + " not found in the repository.");
    }

    public class DockerComposeFileNotFoundException extends Exception {

        public DockerComposeFileNotFoundException(String message) {
            super(message);
        }

    }

}
