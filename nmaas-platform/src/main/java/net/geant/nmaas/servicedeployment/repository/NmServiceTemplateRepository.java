package net.geant.nmaas.servicedeployment.repository;

import net.geant.nmaas.servicedeployment.nmservice.NmServiceTemplate;
import net.geant.nmaas.servicedeployment.orchestrators.dockerswarm.DockerSwarmNmServiceTemplate;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
@Singleton
public class NmServiceTemplateRepository {

    private Map<String, NmServiceTemplate> templates = new HashMap<>();

    {
        templates = new HashMap<>();
        templates.put("tomcat-on-alpine", new DockerSwarmNmServiceTemplate("tomcat-on-alpine", "tomcat:alpine"));
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
