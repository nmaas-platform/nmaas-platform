package net.geant.nmaas.servicedeployment;

import net.geant.nmaas.servicedeployment.exceptions.CouldNotConnectToOrchestratorException;
import net.geant.nmaas.servicedeployment.exceptions.OrchestratorInternalErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl/>
 */
@RestController
public class ServiceDeploymentRestController {

    @Autowired
    @Qualifier("DockerEngine")
    private ContainerOrchestrationProvider orchestrator;

    @RequestMapping(value = "/api/services", method = RequestMethod.GET)
    public List<String> services() throws OrchestratorInternalErrorException, CouldNotConnectToOrchestratorException {
        return orchestrator.listServices();
    }

/*    @RequestMapping(value = "/api/services", method = RequestMethod.POST)
    public String deployTestService() throws NotFoundException {

        return orchestrator.deployService();
    }*/

}
