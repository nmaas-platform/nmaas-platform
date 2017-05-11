package net.geant.nmaas.nmservice.deployment.api;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.nmservice.deployment.ContainerOrchestrationProvider;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerOrchestratorInternalErrorException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotConnectToOrchestratorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl/>
 */
@RestController
@RequestMapping(value = "/platform/api/services")
public class NmServiceDeploymentRestController {

    @Autowired
    @Qualifier("DockerEngine")
    private ContainerOrchestrationProvider orchestrator;

    @Autowired
    private DockerHostRepositoryManager dockerHostRepositoryManager;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String basicInfo() {
        return "This is NMaaS Platform REST API for NM Services configuration";
    }

    @RequestMapping(value = "/deployed", method = RequestMethod.GET)
    public List<String> services(HttpServletResponse response) throws DockerHostNotFoundException, ContainerOrchestratorInternalErrorException, CouldNotConnectToOrchestratorException {
        return orchestrator.listServices(dockerHostRepositoryManager.loadPreferredDockerHost());
    }

    @ExceptionHandler(DockerHostNotFoundException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleDockerHostNotFoundException(DockerHostNotFoundException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(ContainerOrchestratorInternalErrorException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleOrchestratorInternalErrorException(ContainerOrchestratorInternalErrorException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(CouldNotConnectToOrchestratorException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleCouldNotConnectToOrchestratorException(CouldNotConnectToOrchestratorException ex) {
        return ex.getMessage();
    }

}
