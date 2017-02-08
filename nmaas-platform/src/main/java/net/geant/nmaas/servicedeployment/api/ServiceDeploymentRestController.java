package net.geant.nmaas.servicedeployment.api;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepository;
import net.geant.nmaas.servicedeployment.ContainerOrchestrationProvider;
import net.geant.nmaas.servicedeployment.exceptions.CouldNotConnectToOrchestratorException;
import net.geant.nmaas.servicedeployment.exceptions.OrchestratorInternalErrorException;
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
@RequestMapping(value = "/api/services")
public class ServiceDeploymentRestController {

    @Autowired
    @Qualifier("DockerEngine")
    private ContainerOrchestrationProvider orchestrator;

    @Autowired
    private DockerHostRepository dockerHostRepository;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String basicInfo() {
        return "This is NMaaS Platform REST API for NM Services configuration";
    }

    @RequestMapping(value = "/deployed", method = RequestMethod.GET)
    public List<String> services(HttpServletResponse response) throws DockerHostNotFoundException, OrchestratorInternalErrorException, CouldNotConnectToOrchestratorException {
        return orchestrator.listServices(dockerHostRepository.loadPreferredDockerHost());
    }

    @ExceptionHandler(DockerHostNotFoundException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleDockerHostNotFoundException(DockerHostNotFoundException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(OrchestratorInternalErrorException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleOrchestratorInternalErrorException(OrchestratorInternalErrorException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(CouldNotConnectToOrchestratorException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleCouldNotConnectToOrchestratorException(CouldNotConnectToOrchestratorException ex) {
        return ex.getMessage();
    }

}
