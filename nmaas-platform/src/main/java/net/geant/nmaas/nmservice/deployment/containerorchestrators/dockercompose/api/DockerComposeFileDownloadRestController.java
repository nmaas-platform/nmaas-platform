package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.api;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeFile;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.exceptions.DockerComposeFileNotFoundException;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.repository.NmServiceInfoRepository;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RestController
@RequestMapping(value = "/platform/api/dockercompose/files")
public class DockerComposeFileDownloadRestController {

    private final static Logger log = LogManager.getLogger(DockerComposeFileDownloadRestController.class);

    @Autowired
    private NmServiceInfoRepository nmServiceInfoRepository;

    @GetMapping(value = "/{deploymentId}")
    public void downloadComposeFile(@PathVariable(value = "deploymentId") String deploymentId, HttpServletResponse response)
            throws MissingElementException, DockerComposeFileNotFoundException, IOException {
        log.info("Received compose file download request (deploymentId -> " + deploymentId + ")");
        NmServiceInfo nmServiceInfo = nmServiceInfoRepository.findByDeploymentId(Identifier.newInstance(deploymentId))
                .orElseThrow(() -> new MissingElementException("NM service info for deployment with id " + deploymentId + " not found."));
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
