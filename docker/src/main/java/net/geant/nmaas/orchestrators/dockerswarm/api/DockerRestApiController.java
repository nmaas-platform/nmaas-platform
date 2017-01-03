package net.geant.nmaas.orchestrators.dockerswarm.api;

import com.spotify.docker.client.exceptions.NotFoundException;
import com.spotify.docker.client.messages.Info;
import com.spotify.docker.client.messages.swarm.Service;
import net.geant.nmaas.orchestrators.dockerswarm.service.ServicesManager;
import net.geant.nmaas.orchestrators.dockerswarm.service.StatusManager;
import org.springframework.beans.factory.annotation.Autowired;
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
    private StatusManager status;
    @Autowired
    private ServicesManager services;

    @RequestMapping(value = "/api/info", method = RequestMethod.GET)
    public Info info() throws NotFoundException {
       return status.info();
    }

    @RequestMapping(value = "/api/services", method = RequestMethod.GET)
    public List<Service> services() throws NotFoundException {
        return services.listServices();
    }

    @RequestMapping(value = "/api/services", method = RequestMethod.POST)
    public String deployTestService() throws NotFoundException {
        return services.deployTestService();
    }

}
