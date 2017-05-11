package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.repositories;

import freemarker.template.Template;
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
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DockerComposeTemplateRepositoryTest {

    private static final String TEST_APP_NAME = "testAppName";
    private static final String TEST_APP_VERSION = "1.0.0";
    private static final String OXIDIZED_APP_NAME = "Oxidized";
    private static final String OXIDIZED_APP_VERSION = "0.19.0";

    @Autowired
    private DockerComposeFileTemplateRepository templateRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    private Long validTestAppId;

    private Long wrongTestAppId;

    @Before
    public void setup() {
        Application app = new Application(OXIDIZED_APP_NAME);
        app.setVersion(OXIDIZED_APP_VERSION);
        validTestAppId = applicationRepository.save(app).getId();
        app = new Application(OXIDIZED_APP_NAME);
        app.setVersion(OXIDIZED_APP_VERSION + "wrongVersion");
        wrongTestAppId = applicationRepository.save(app).getId();
    }

    @Test
    public void shouldReturnComposeFileTemplateForOxidizedApp() throws DockerComposeTemplateHandlingException {
        Template template = templateRepository.loadTemplate(Identifier.newInstance(String.valueOf(validTestAppId)));
        assertThat(template.getName(), endsWith("docker-compose.yml-template"));
    }

    @Test(expected = DockerComposeTemplateHandlingException.class)
    public void shouldThrowExceptionOnMissingComposeFileTemplateDirectory() throws DockerComposeTemplateHandlingException {
        templateRepository.loadTemplate(Identifier.newInstance(String.valueOf(wrongTestAppId)));
    }

    @Test
    public void shouldConstructProperComposeFileTemplateDirectoryForTestApplication() {
        Application app = new Application(TEST_APP_NAME);
        assertThat(templateRepository.constructConfigDirectoryForApplication(app), equalTo(TEST_APP_NAME));
        app.setVersion(TEST_APP_VERSION);
        assertThat(templateRepository.constructConfigDirectoryForApplication(app), equalTo(TEST_APP_NAME + "-" + TEST_APP_VERSION));
    }

    @After
    public void removeTestAppFromDatabase() {
        applicationRepository.delete(validTestAppId);
        applicationRepository.delete(wrongTestAppId);
    }

}
