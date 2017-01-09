package net.geant.nmaas;

import com.spotify.docker.client.exceptions.NotFoundException;
import com.spotify.docker.client.messages.Info;
import com.spotify.docker.client.messages.swarm.Service;
import net.geant.nmaas.exception.CouldNotConnectToOrchestratorException;
import net.geant.nmaas.exception.OrchestratorInternalErrorException;
import net.geant.nmaas.orchestrators.dockerswarm.service.ServicesManager;
import net.geant.nmaas.orchestrators.dockerswarm.service.StatusManager;
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
public class DockerRestApiController {

    @Autowired
    @Qualifier("DockerSwarm")
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
