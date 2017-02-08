package net.geant.nmaas.nmservicedeployment.repository;

import net.geant.nmaas.nmservicedeployment.containerorchestrators.dockerengine.DockerEngineContainerTemplate;
import net.geant.nmaas.nmservicedeployment.containerorchestrators.dockerengine.container.ContainerPortForwardingSpec;
import net.geant.nmaas.nmservicedeployment.containerorchestrators.dockerswarm.DockerSwarmNmServiceTemplate;
import net.geant.nmaas.nmservicedeployment.nmservice.NmServiceTemplate;
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
        templates.put(tomcatTemplate.getName(), tomcatTemplate);

        DockerEngineContainerTemplate oxidizedTemplate = new DockerEngineContainerTemplate("oxidized", "oxidized/oxidized:latest");
        oxidizedTemplate.setCommandInSpecRequired(false);
        oxidizedTemplate.setEnv(asList("CONFIG_RELOAD_INTERVAL: 600"));
        oxidizedTemplate.setEnvVariablesInSpecRequired(false);
        oxidizedTemplate.setExposedPorts(asList(new ContainerPortForwardingSpec("ui", ContainerPortForwardingSpec.Protocol.TCP, 8888)));
        templates.put(oxidizedTemplate.getName(), oxidizedTemplate);

        DockerEngineContainerTemplate pmacctGrafanaTemplate = new DockerEngineContainerTemplate("pmacct-grafana", "llopat/pmacct");
        pmacctGrafanaTemplate.setExposedPorts(asList(new ContainerPortForwardingSpec("ui", ContainerPortForwardingSpec.Protocol.TCP, 3000)));
        pmacctGrafanaTemplate.setCommandInSpecRequired(false);
        pmacctGrafanaTemplate.setEnvVariablesInSpecRequired(false);
        pmacctGrafanaTemplate.setContainerVolumes(asList("/data"));
        templates.put(pmacctGrafanaTemplate.getName(), pmacctGrafanaTemplate);

       // docker create --name check-vlan--pmacct-1 -p 5001:3000 -v /home/mgmt/docker/data-pmacct-1:/data llopat/pmacct "$@"

        DockerSwarmNmServiceTemplate tomcatOnSwarmTemplate = new DockerSwarmNmServiceTemplate("tomcat-on-swarm-alpine", "tomcat:alpine");
        templates.put(tomcatOnSwarmTemplate.getName(), tomcatOnSwarmTemplate);
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
