package net.geant.nmaas.nmservice.configuration.repository;

import freemarker.template.Template;
import net.geant.nmaas.nmservice.configuration.exceptions.ConfigTemplateHandlingException;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback
public class NmServiceConfigurationTemplatesRepositoryTest {

    private static final String TEST_APP_NAME = "testAppName";
    private static final String TEST_APP_VERSION = "1.0.0";
    private static final String OXIDIZED_APP_NAME = "Oxidized";
    private static final String OXIDIZED_APP_VERSION = "0.19.0";

    @Autowired
    private NmServiceConfigurationTemplatesRepository templatesRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Before
    public void setup() {
        Application app = new Application(OXIDIZED_APP_NAME);
        app.setVersion(OXIDIZED_APP_VERSION);
        applicationRepository.save(app);
    }

    @Test
    public void shouldReturnListOfTwoConfigTemplatesForOxidizedApp() throws ConfigTemplateHandlingException {
        Identifier oxidizedIdentifier = Identifier.newInstance(String.valueOf(applicationRepository.findByName(OXIDIZED_APP_NAME).get(0).getId()));
        List<Template> templates = templatesRepository.loadTemplates(oxidizedIdentifier);
        assertThat(templates.size(), equalTo(2));
        assertThat(templates.stream().map(t -> t.getName()).filter(n -> (n.endsWith("config-template") || n.endsWith("router.db-template"))).count(), equalTo(2L));
    }

    @Test
    public void shouldConstructProperConfigDirectoryForTestApplication() {
        Application app = new Application(TEST_APP_NAME);
        assertThat(templatesRepository.constructConfigDirectoryForApplication(app), equalTo(TEST_APP_NAME));
        app.setVersion(TEST_APP_VERSION);
        assertThat(templatesRepository.constructConfigDirectoryForApplication(app), equalTo(TEST_APP_NAME + "-" + TEST_APP_VERSION));
    }

}
