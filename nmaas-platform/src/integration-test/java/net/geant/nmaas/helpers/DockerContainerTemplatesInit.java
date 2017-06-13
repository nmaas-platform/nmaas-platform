package net.geant.nmaas.helpers;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerPortForwarding;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerTemplate;

import java.util.Arrays;

public class DockerContainerTemplatesInit {

    public static DockerContainerTemplate oxidizedTemplate() {
        DockerContainerTemplate oxidizedTemplate =
                new DockerContainerTemplate("oxidized/oxidized:latest");
        oxidizedTemplate.setEnvVariables(Arrays.asList("CONFIG_RELOAD_INTERVAL=600"));
        oxidizedTemplate.setExposedPort(new DockerContainerPortForwarding(DockerContainerPortForwarding.Protocol.TCP, 8888));
        oxidizedTemplate.setContainerVolumes(Arrays.asList("/root/.config/oxidized"));
        return oxidizedTemplate;
    }

    public static DockerContainerTemplate alpineTomcatTemplate() {
        DockerContainerTemplate tomcatTemplate =
                new DockerContainerTemplate("tomcat:alpine");
        tomcatTemplate.setExposedPort(new DockerContainerPortForwarding(DockerContainerPortForwarding.Protocol.TCP, 8080));
        return tomcatTemplate;
    }

    public static DockerContainerTemplate pmacctGrafanaTemplate() {
        DockerContainerTemplate pmacctGrafanaTemplate =
                new DockerContainerTemplate("llopat/pmacct");
        pmacctGrafanaTemplate.setExposedPort(new DockerContainerPortForwarding(DockerContainerPortForwarding.Protocol.TCP, 3000));
        pmacctGrafanaTemplate.setContainerVolumes(Arrays.asList("/data"));
        return pmacctGrafanaTemplate;
    }

}
