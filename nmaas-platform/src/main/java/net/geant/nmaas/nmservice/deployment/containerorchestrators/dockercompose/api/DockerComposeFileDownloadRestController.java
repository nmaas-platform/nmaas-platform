package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.api;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.repositories.DockerComposeFile;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.repositories.DockerComposeFileRepository;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RestController
@RequestMapping(value = "/platform/api/dockercompose/files")
public class DockerComposeFileDownloadRestController {

    private final static Logger log = LogManager.getLogger(DockerComposeFileDownloadRestController.class);

    @Autowired
    private DockerComposeFileRepository composeFiles;

    @RequestMapping(value = "/{deploymentId}", method = RequestMethod.GET)
    public void downloadComposeFile(@PathVariable String deploymentId, HttpServletResponse response)
            throws DockerComposeFileRepository.DockerComposeFileNotFoundException, IOException {
        log.info("Received compose file download request (deploymentId -> " + deploymentId + ")");
        final DockerComposeFile composeFile = composeFiles.loadFileContent(Identifier.newInstance(deploymentId));
        response.setCharacterEncoding("UTF-8");
        response.addHeader("Content-disposition", "attachment;filename=" + DockerComposeFile.DEFAULT_DOCKER_COMPOSE_FILE_NAME);
        response.setContentType("application/octet-stream");
        IOUtils.copy(new ByteArrayInputStream(composeFile.getComposeFileContent()), response.getOutputStream());
        response.flushBuffer();
    }

    @ExceptionHandler(DockerComposeFileRepository.DockerComposeFileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleConfigurationNotFoundException(DockerComposeFileRepository.DockerComposeFileNotFoundException ex) {
        log.warn("Requested compose file not found -> " + ex.getMessage());
        return ex.getMessage();
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleIOException(IOException ex) {
        log.warn("Failed to find and return requested compose file -> " + ex.getMessage());
        return ex.getMessage();
    }
}
