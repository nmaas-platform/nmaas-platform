package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DcnAttachedContainer;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeFileTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeService;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeServiceComponent;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.repositories.DockerComposeFileRepository;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

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
    private DockerComposeFileRepository composeFileRepository;
    @Autowired
    private AppDeploymentRepository appDeploymentRepository;

    private Identifier deploymentId = Identifier.newInstance("deploymentId");
    private Identifier clientId = Identifier.newInstance("clientId");
    private Identifier applicationId = Identifier.newInstance("applicationId");
    private DockerComposeFileTemplate template;

    @Before
    public void setup() {
        prepareTestComposeFileTemplate();
        storeTestAppDeployment();
    }

    private void prepareTestComposeFileTemplate() {
        String composeFileTemplateContent = "version: \\\"2\\\"\\n\\nservices:\\n  ${container.container_name}:\\n    restart: always\\n    image: oxidized/oxidized:latest\\n    ports:\\n      - ${port}:8888/tcp\\n    environment:\\n      CONFIG_RELOAD_INTERVAL: 600\\n    volumes:\\n      - ${volume}:/root/.config/oxidized\\n    networks:\\n      nmaas-ext-access:\\n      nmaas-dcn:\\n        ipv4_address: ${container.container_ip_address}\\n    privileged: true\\n\\nnetworks:\\n  nnmaas-ext-access:\\n    external:\\n      name: ${nmaas_ext_access_network}\\n  nmaas-dcn:\\n    external:\\n      name: ${nmaas_dcn_network}";
        template = new DockerComposeFileTemplate();
        template.setDcnAttachedContainers(Arrays.asList(new DcnAttachedContainer("container", "test container")));
        template.setComposeFileTemplateContent(composeFileTemplateContent);
    }

    private void storeTestAppDeployment() {
        AppDeploymentSpec appDeploymentSpec = new AppDeploymentSpec();
        appDeploymentSpec.setDockerComposeFileTemplate(template);
        AppDeployment deployment = new AppDeployment(deploymentId, clientId, applicationId);
        appDeploymentRepository.save(deployment);
    }

    @After
    public void clean() {
        appDeploymentRepository.deleteAll();
    }

    @Test
    public void shouldBuildAndStoreComposeFile() throws Exception {
        DockerComposeService input = new DockerComposeService();
        input.setPublicPort(5000);
        input.setAttachedVolumeName("/home/dir");
        input.setExternalAccessNetworkName("testExtNetwork");
        input.setDcnNetworkName("testDcnNetwork");
        DockerComposeServiceComponent component = new DockerComposeServiceComponent();
        component.setName("container");
        component.setDeploymentName("containerName1");
        component.setIpAddressOfContainer("1.2.3.4");
        input.setServiceComponents(Arrays.asList(component));
        composeFilePreparer.buildAndStoreComposeFile(deploymentId, input, template);
        assertThat(contentOfGeneratedComposeFile(), allOf(
                        containsString("5000:"),
                        containsString("/home/dir:"),
                        containsString("containerName1"),
                        containsString("1.2.3.4"),
                        containsString("testExtNetwork"),
                        containsString("testDcnNetwork")));
        assertThat(composeFileRepository.count(), equalTo(1L));
        appDeploymentRepository.deleteAll();
        assertThat(composeFileRepository.count(), equalTo(0L));
    }

    private String contentOfGeneratedComposeFile() {
        return appDeploymentRepository.findByDeploymentId(deploymentId).get().getDockerComposeFile().getComposeFileContent();
    }

}
