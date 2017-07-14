package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeFileTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.repositories.DockerComposeFileRepository;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.repositories.DockerComposeFileTemplateRepository;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

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
    private DockerComposeFileTemplateRepository composeFileTemplateRepository;

    private Identifier applicationId = Identifier.newInstance("1");
    private Identifier deploymentId = Identifier.newInstance("deploymentId");

    @Before
    public void setup() {
        String composeFileTemplateContent = "version: \\\"2\\\"\\n\\nservices:\\n  ${container_name}:\\n    restart: always\\n    image: oxidized/oxidized:latest\\n    ports:\\n      - ${port}:8888/tcp\\n    environment:\\n      CONFIG_RELOAD_INTERVAL: 600\\n    volumes:\\n      - ${volume}:/root/.config/oxidized\\n    networks:\\n      nmaas-ext-access:\\n      nmaas-dcn:\\n        ipv4_address: ${container_ip_address}\\n    privileged: true\\n\\nnetworks:\\n  nnmaas-ext-access:\\n    external:\\n      name: ${nmaas_ext_access_network}\\n  nmaas-dcn:\\n    external:\\n      name: ${nmaas_dcn_network}";
        DockerComposeFileTemplate template = new DockerComposeFileTemplate(applicationId.longValue(), composeFileTemplateContent);
        composeFileTemplateRepository.save(template);
    }

    @After
    public void clean() {
        composeFileTemplateRepository.deleteAll();
        composeFileRepository.deleteAll();
    }

    @Test
    public void shouldBuildComposeFile() throws Exception {
        DockerComposeFileInput input = new DockerComposeFileInput(5000, "/home/dir");
        input.setContainerName(deploymentId.value());
        input.setContainerIpAddress("");
        input.setExternalAccessNetworkName("");
        input.setDcnNetworkName("");
        composeFilePreparer.buildAndStoreComposeFile(deploymentId, Identifier.newInstance(String.valueOf("1")), input);
        assertThat(composeFileRepository.findByDeploymentId(deploymentId).get().getComposeFileContent(),
                allOf(containsString("5000:"), containsString("/home/dir:")));
    }

}
