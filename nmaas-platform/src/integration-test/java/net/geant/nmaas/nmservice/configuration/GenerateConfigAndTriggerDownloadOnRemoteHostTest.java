package net.geant.nmaas.nmservice.configuration;

import freemarker.template.Template;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepository;
import net.geant.nmaas.nmservice.configuration.exceptions.ConfigTemplateHandlingException;
import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfigurationTemplatesRepository;
import net.geant.nmaas.orchestration.Identifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static net.geant.nmaas.orchestration.AppLifecycleManager.OXIDIZED_APPLICATION_ID;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class GenerateConfigAndTriggerDownloadOnRemoteHostTest {

    @Autowired
    private SimpleNmServiceConfigurationExecutor configurationExecutor;

    @Autowired
    private DockerHostRepository dockerHostRepository;

    @Autowired
    private NmServiceConfigurationTemplatesRepository templatesRepository;

    @Test(expected = ConfigTemplateHandlingException.class)
    public void shouldGenerateConfigAndTriggerDownloadOnRemoteHost() throws Exception {
        final Identifier deploymentId = Identifier.newInstance("testDeploymentId");
        final DockerHost host = dockerHostRepository.loadPreferredDockerHost();
        final Template configTemplate = templatesRepository.loadTemplates(OXIDIZED_APPLICATION_ID).get(0);
        configurationExecutor.generateConfigAndTriggerDownloadOnRemoteHost(deploymentId, configTemplate, null, host);
    }

}
