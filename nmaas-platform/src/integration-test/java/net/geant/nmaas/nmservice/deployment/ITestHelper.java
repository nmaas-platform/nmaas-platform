package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerContainerPortForwarding;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerContainerTemplate;

import java.util.Arrays;

public class ITestHelper {

    public static DockerContainerTemplate oxidizedTemplate() {
        DockerContainerTemplate oxidizedTemplate =
                new DockerContainerTemplate("oxidized/oxidized:latest");
        oxidizedTemplate.setEnvVariables(Arrays.asList("CONFIG_RELOAD_INTERVAL=600"));
        oxidizedTemplate.setEnvVariablesInSpecRequired(false);
        oxidizedTemplate.setExposedPort(new DockerContainerPortForwarding(DockerContainerPortForwarding.Protocol.TCP, 8888));
        oxidizedTemplate.setContainerVolumes(Arrays.asList("/root/.config/oxidized"));
        return oxidizedTemplate;
    }

    public static DockerContainerTemplate alpineTomcatTemplate() {
        DockerContainerTemplate tomcatTemplate =
                new DockerContainerTemplate("tomcat:alpine");
        return tomcatTemplate;
    }

    public static DockerContainerTemplate pmacctGrafanaTemplate() {
        DockerContainerTemplate pmacctGrafanaTemplate =
                new DockerContainerTemplate("llopat/pmacct");
        pmacctGrafanaTemplate.setExposedPort(new DockerContainerPortForwarding(DockerContainerPortForwarding.Protocol.TCP, 3000));
        pmacctGrafanaTemplate.setEnvVariablesInSpecRequired(false);
        pmacctGrafanaTemplate.setContainerVolumes(Arrays.asList("/data"));
        return pmacctGrafanaTemplate;
    }

}
