package net.geant.nmaas.nmservice.configuration.repositories;

import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfigurationTemplate;
import net.geant.nmaas.nmservice.configuration.exceptions.ConfigTemplateHandlingException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class NmServiceConfigurationTemplatesRepositoryTest {

    @Autowired
    private NmServiceConfigFileTemplatesRepository templatesRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    private Long testAppId;

    @Before
    public void setup() {
        Application app = new Application("oxidizedAppName");
        app.setVersion("oxidizedAppVersion");
        testAppId = applicationRepository.save(app).getId();
        NmServiceConfigurationTemplate oxidizedConfigTemplate1 = new NmServiceConfigurationTemplate();
        oxidizedConfigTemplate1.setApplicationId(testAppId);
        oxidizedConfigTemplate1.setConfigFileName("config");
        oxidizedConfigTemplate1.setConfigFileTemplateContent("");
        templatesRepository.save(oxidizedConfigTemplate1);
        NmServiceConfigurationTemplate oxidizedConfigTemplate2 = new NmServiceConfigurationTemplate();
        oxidizedConfigTemplate2.setApplicationId(testAppId);
        oxidizedConfigTemplate2.setConfigFileName("router.db");
        oxidizedConfigTemplate2.setConfigFileTemplateContent("");
        templatesRepository.save(oxidizedConfigTemplate2);
    }

    @Test
    public void shouldReturnListOfTwoConfigTemplatesForOxidizedApp() throws ConfigTemplateHandlingException {
        List<NmServiceConfigurationTemplate> templates = templatesRepository.findAllByApplicationId(testAppId);
        assertThat(templates.size(), equalTo(2));
        assertThat(
                templates.stream().map(template -> template.getConfigFileName()).collect(Collectors.toList()),
                contains("config", "router.db"));
    }

    @After
    public void removeTestAppFromDatabase() {
        applicationRepository.deleteAll();
        templatesRepository.deleteAll();
    }

}
