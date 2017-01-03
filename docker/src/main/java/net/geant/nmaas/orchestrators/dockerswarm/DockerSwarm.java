package net.geant.nmaas.orchestrators.dockerswarm;

import net.geant.nmaas.ContainerOrchestrationProvider;
import net.geant.nmaas.nmservice.NmServiceSpec;
import net.geant.nmaas.nmservice.NmServiceTemplate;
import net.geant.nmaas.orchestrators.dockerswarm.service.ServicesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service("DockerSwarm")
public class DockerSwarm implements ContainerOrchestrationProvider {

    @Autowired
    private ServicesManager services;

    @Override
    public void deployNmService(NmServiceTemplate template, NmServiceSpec spec) {
        //TODO
    }

    @Override
    public void verifyService(String serviceName) {
        //TODO
    }

    @Override
    public void destroyNmService(String serviceName) {
        //TODO
    }

    @Override
    public String info() {
        return "DockerSwarm Container Orchestrator";
    }
}
