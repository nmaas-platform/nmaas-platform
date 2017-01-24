package net.geant.nmaas.servicedeployment.repository;

import net.geant.nmaas.servicedeployment.nmservice.NmServiceTemplate;
import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.DockerEngineContainerTemplate;
import net.geant.nmaas.servicedeployment.orchestrators.dockerswarm.DockerSwarmNmServiceTemplate;
import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.container.PortForwardingSpec;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
@Singleton
public class NmServiceTemplateRepository {

    private Map<String, NmServiceTemplate> templates = new HashMap<>();

    {
        DockerEngineContainerTemplate tomcatTemplate = new DockerEngineContainerTemplate("tomcat-alpine", "tomcat:alpine");
        templates.put("tomcat-alpine", tomcatTemplate);

        DockerEngineContainerTemplate oxidizedTemplate = new DockerEngineContainerTemplate("oxidized", "oxidized/oxidized:latest");
        oxidizedTemplate.setCommandInSpecRequired(false);
        oxidizedTemplate.setEnv(asList("CONFIG_RELOAD_INTERVAL: 600"));
        oxidizedTemplate.setEnvVariablesInSpecRequired(false);
        oxidizedTemplate.setPorts(asList(new PortForwardingSpec("ui", PortForwardingSpec.Protocol.TCP, 8888, 8888)));
        oxidizedTemplate.setPortsInSpecRequired(false);
        templates.put("oxidized", oxidizedTemplate);

        DockerSwarmNmServiceTemplate tomcatOnSwarmTemplate = new DockerSwarmNmServiceTemplate("tomcat-alpine", "tomcat:alpine");
        templates.put("tomcat-on-swarm-alpine", tomcatOnSwarmTemplate);
    }

    public synchronized NmServiceTemplate loadTemplate(String name) {
        return templates.get(name);
    }

    public synchronized void storeTemplete(NmServiceTemplate template) {
        if(template != null && template.getName() != null)
            templates.put(template.getName(), template);
    }

    public synchronized void deleteTemplate(String name) {
        templates.remove(name);
    }

}
