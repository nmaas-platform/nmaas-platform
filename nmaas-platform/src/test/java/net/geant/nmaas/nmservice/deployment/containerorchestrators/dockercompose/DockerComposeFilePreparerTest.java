package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeFileTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.repositories.DockerComposeFileRepository;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DockerComposeFilePreparerTest {

    @Autowired
    private DockerComposeFilePreparer composeFilePreparer;
    @Autowired
    private AppDeploymentRepository appDeploymentRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private DockerComposeFileRepository composeFileRepository;

    private Long appId;
    private Identifier deploymentId = Identifier.newInstance("deploymentId");
    private Identifier clientId = Identifier.newInstance("clientId");
    private Identifier applicationId = Identifier.newInstance("applicationId");

    @Before
    public void setup() {
        String composeFileTemplateContent = "version: \\\"2\\\"\\n\\nservices:\\n  ${container_name}:\\n    restart: always\\n    image: oxidized/oxidized:latest\\n    ports:\\n      - ${port}:8888/tcp\\n    environment:\\n      CONFIG_RELOAD_INTERVAL: 600\\n    volumes:\\n      - ${volume}:/root/.config/oxidized\\n    networks:\\n      nmaas-ext-access:\\n      nmaas-dcn:\\n        ipv4_address: ${container_ip_address}\\n    privileged: true\\n\\nnetworks:\\n  nnmaas-ext-access:\\n    external:\\n      name: ${nmaas_ext_access_network}\\n  nmaas-dcn:\\n    external:\\n      name: ${nmaas_dcn_network}";
        DockerComposeFileTemplate template = new DockerComposeFileTemplate(composeFileTemplateContent);
        Application application = new Application("testApp");
        AppDeploymentSpec appDeploymentSpec = new AppDeploymentSpec();
        appDeploymentSpec.setDockerComposeFileTemplate(template);
        application.setAppDeploymentSpec(appDeploymentSpec);
        appId = applicationRepository.save(application).getId();
        AppDeployment deployment = new AppDeployment(deploymentId, clientId, applicationId);
        appDeploymentRepository.save(deployment);
    }

    @After
    public void clean() {
        applicationRepository.deleteAll();
        appDeploymentRepository.deleteAll();
    }

    @Test
    public void shouldBuildComposeFile() throws Exception {
        DockerComposeFileInput input = new DockerComposeFileInput(5000, "/home/dir");
        input.setContainerName(deploymentId.value());
        input.setContainerIpAddress("");
        input.setExternalAccessNetworkName("");
        input.setDcnNetworkName("");
        composeFilePreparer.buildAndStoreComposeFile(deploymentId, Identifier.newInstance(String.valueOf(appId)), input);
        assertThat(appDeploymentRepository.findByDeploymentId(deploymentId).get().getDockerComposeFile().getComposeFileContent(),
                allOf(containsString("5000:"), containsString("/home/dir:")));
        assertThat(composeFileRepository.count(), equalTo(1L));
        appDeploymentRepository.deleteAll();
        assertThat(composeFileRepository.count(), equalTo(0L));
    }

}
