package net.geant.nmaas.nmservice.deployment.repository;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerEngineContainerTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.container.ContainerPortForwardingSpec;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceTemplate;
import net.geant.nmaas.orchestration.Identifier;
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

    public static final Identifier OXIDIZED_APPLICATION_ID = Identifier.newInstance("oxidizedApplicationId");

    public static final Identifier TOMCAT_ALPINE_APPLICATION_ID = Identifier.newInstance("tomcatAlpineApplicationId");

    public static final Identifier PMACCT_GRAFANA_APPLICATION_ID = Identifier.newInstance("pmacctGrafanaApplicationId");

    private Map<Identifier, NmServiceTemplate> templates = new HashMap<>();

    {
        DockerEngineContainerTemplate tomcatTemplate =
                new DockerEngineContainerTemplate(TOMCAT_ALPINE_APPLICATION_ID, "tomcat-alpine", "tomcat:alpine");
        templates.put(TOMCAT_ALPINE_APPLICATION_ID, tomcatTemplate);

        DockerEngineContainerTemplate oxidizedTemplate =
                new DockerEngineContainerTemplate(OXIDIZED_APPLICATION_ID, "oxidized", "oxidized/oxidized:latest");
        oxidizedTemplate.setCommandInSpecRequired(false);
        oxidizedTemplate.setEnv(asList("CONFIG_RELOAD_INTERVAL: 600"));
        oxidizedTemplate.setEnvVariablesInSpecRequired(false);
        oxidizedTemplate.setExposedPorts(asList(new ContainerPortForwardingSpec("ui", ContainerPortForwardingSpec.Protocol.TCP, 8888)));
        templates.put(OXIDIZED_APPLICATION_ID, oxidizedTemplate);

        DockerEngineContainerTemplate pmacctGrafanaTemplate =
                new DockerEngineContainerTemplate(PMACCT_GRAFANA_APPLICATION_ID, "pmacct-grafana", "llopat/pmacct");
        pmacctGrafanaTemplate.setExposedPorts(asList(new ContainerPortForwardingSpec("ui", ContainerPortForwardingSpec.Protocol.TCP, 3000)));
        pmacctGrafanaTemplate.setCommandInSpecRequired(false);
        pmacctGrafanaTemplate.setEnvVariablesInSpecRequired(false);
        pmacctGrafanaTemplate.setContainerVolumes(asList("/data"));
        templates.put(PMACCT_GRAFANA_APPLICATION_ID, pmacctGrafanaTemplate);
    }

    public NmServiceTemplate loadTemplateByName(String name) {
        return templates.values().stream().filter(template -> template.getName().equals(name)).findFirst().get();
    }

    public NmServiceTemplate loadTemplateByApplicationId(Identifier applicationId) {
        return templates.get(applicationId);
    }

    public synchronized void storeTemplate(Identifier applicationId, NmServiceTemplate template) {
        if(template != null && applicationId != null)
            templates.put(applicationId, template);
    }

    public synchronized void deleteTemplate(String name) {
        templates.remove(name);
    }
}
