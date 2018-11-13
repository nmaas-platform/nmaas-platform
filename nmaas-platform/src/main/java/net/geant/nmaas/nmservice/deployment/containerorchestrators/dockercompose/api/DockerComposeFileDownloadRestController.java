package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.api;

import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.DockerComposeServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeFile;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.exceptions.DockerComposeFileNotFoundException;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("env_docker-compose")
@RequestMapping(value = "/api/dockercompose/files")
@Log4j2
public class DockerComposeFileDownloadRestController {

    private DockerComposeServiceRepositoryManager repositoryManager;

    @Autowired
    public DockerComposeFileDownloadRestController(DockerComposeServiceRepositoryManager repositoryManager){
        this.repositoryManager = repositoryManager;
    }

    @GetMapping(value = "/{deploymentId}")
    public void downloadComposeFile(@PathVariable(value = "deploymentId") String deploymentId, HttpServletResponse response)
            throws IOException {
        log.info("Received compose file download request (deploymentId -> " + deploymentId + ")");
        DockerComposeNmServiceInfo nmServiceInfo = null;
        try {
            nmServiceInfo = repositoryManager.loadService(Identifier.newInstance(deploymentId));
        } catch (InvalidDeploymentIdException e) {
            throw new MissingElementException("NM service info for deployment with id " + deploymentId + " not found.");
        }
        DockerComposeFile composeFile = nmServiceInfo.getDockerComposeFile();
        if (composeFile == null)
            throw new DockerComposeFileNotFoundException("Docker Compose file for application deployment with id " + deploymentId + " is missing.");
        response.setCharacterEncoding("UTF-8");
        response.addHeader("Content-disposition", "attachment;filename=" + DockerComposeFile.DEFAULT_DOCKER_COMPOSE_FILE_NAME);
        response.setContentType("application/octet-stream");
        response.getOutputStream().write(composeFile.getComposeFileContent().getBytes(Charset.forName("UTF-8")));
        response.flushBuffer();
    }

    @ExceptionHandler(DockerComposeFileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleComposeFileNotFoundException(DockerComposeFileNotFoundException ex) {
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
