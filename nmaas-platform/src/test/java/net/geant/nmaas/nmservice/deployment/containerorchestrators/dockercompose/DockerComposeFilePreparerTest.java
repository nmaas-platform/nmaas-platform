package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.repositories.DockerComposeFileRepository;
import net.geant.nmaas.orchestration.entities.Identifier;
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
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DockerComposeFilePreparerTest {

    private static final String OXIDIZED_APP_NAME = "Oxidized";
    private static final String OXIDIZED_APP_VERSION = "0.19.0";

    @Autowired
    private DockerComposeFilePreparer composeFilePreparer;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private DockerComposeFileRepository composeFileRepository;

    private Identifier deploymentId = Identifier.newInstance("deploymentId");

    private Long testAppId;

    @Before
    public void setup() {
        Application app = new Application(OXIDIZED_APP_NAME);
        app.setVersion(OXIDIZED_APP_VERSION);
        testAppId = applicationRepository.save(app).getId();
    }

    @After
    public void removeTestAppFromDatabase() {
        applicationRepository.delete(testAppId);
    }

    @Test
    public void shouldBuildComposeFile() throws Exception {
        composeFilePreparer.buildAndStoreComposeFile(deploymentId, Identifier.newInstance(String.valueOf(testAppId)), 50, "/home/dir");
        assertThat(new String(composeFileRepository.loadFileContent(deploymentId).getComposeFileContent(), "UTF-8"),
                allOf(containsString("50:"), containsString("/home/dir:")));
    }

}
