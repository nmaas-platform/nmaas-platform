package net.geant.nmaas.nmservice.configuration;

import freemarker.template.Template;
import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfiguration;
import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfigurationTemplatesRepository;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static net.geant.nmaas.nmservice.configuration.repository.NmServiceConfigurationTemplatesRepository.DEFAULT_TEMPLATE_FILE_NAME_SUFFIX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class NmServiceConfigurationOxidizedTemplatesTest {

    private static final String TEST_CONFIG_ID_1 = "1";
    private static final String TEST_CONFIG_ID_2 = "2";
    private static final String TEST_TEMPLATE_NAME_1 = "config-template";
    private static final String TEST_TEMPLATE_NAME_2 = "router.db-template";
    private static final String OXIDIZED_APP_NAME = "Oxidized";
    private static final String OXIDIZED_APP_VERSION = "0.19.0";

    @Autowired
    private NmServiceConfigurationTemplatesRepository templatesRepository;

    @Autowired
    private NmServiceConfigurationsPreparer configurationsPreparer;

    @Autowired
    private ApplicationRepository applicationRepository;

    private Long testAppId;

    @Before
    public void setup() {
        Application app = new Application(OXIDIZED_APP_NAME);
        app.setVersion(OXIDIZED_APP_VERSION);
        testAppId = applicationRepository.save(app).getId();
    }

    @Test
    public void shouldPopulateAndPrintConfigurationFile() throws Exception {
        Identifier oxidizedIdentifier = Identifier.newInstance(String.valueOf(applicationRepository.findByName(OXIDIZED_APP_NAME).get(0).getId()));
        List<Template> templates = templatesRepository.loadTemplates(oxidizedIdentifier);
        assertThat(templates.size(), equalTo(2));
        assertThat(templates.get(0).getName(), endsWith(DEFAULT_TEMPLATE_FILE_NAME_SUFFIX));
        assertThat(SimpleNmServiceConfigurationHelper.configFileNameFromTemplateName(templates.get(0).getName()),
                not(containsString(DEFAULT_TEMPLATE_FILE_NAME_SUFFIX)));
    }

    @Test
    public void shouldBuildConfigFromTemplateAndUserProvidedInput() throws Exception {
        Identifier oxidizedIdentifier = Identifier.newInstance(String.valueOf(applicationRepository.findByName(OXIDIZED_APP_NAME).get(0).getId()));
        List<Template> templates = templatesRepository.loadTemplates(oxidizedIdentifier);
        Optional<Template> tut = templates.stream().filter(t -> t.getName().endsWith(TEST_TEMPLATE_NAME_1)).findFirst();
        NmServiceConfiguration nmServiceConfiguration =
                configurationsPreparer.buildConfigFromTemplateAndUserProvidedInput(
                        TEST_CONFIG_ID_1,
                        tut.orElseThrow(() -> new Exception()),
                        testOxidizedDefaultConfigurationInputModel());
        assertThat(nmServiceConfiguration.getConfigFileName(), equalTo("config"));
        assertThat(new String(nmServiceConfiguration.getConfigFileContent(), "UTF-8"),
                Matchers.allOf(containsString("user123"), containsString("pass123")));
        tut = templates.stream().filter(t -> t.getName().endsWith(TEST_TEMPLATE_NAME_2)).findFirst();
        nmServiceConfiguration =
                configurationsPreparer.buildConfigFromTemplateAndUserProvidedInput(
                        TEST_CONFIG_ID_2,
                        tut.orElseThrow(() -> new Exception()),
                        testOxidizedDefaultConfigurationInputModel());
        assertThat(nmServiceConfiguration.getConfigFileName(), equalTo("router.db"));
        assertThat(new String(nmServiceConfiguration.getConfigFileContent(), "UTF-8"),
                Matchers.allOf(containsString("7.7.7.7"), containsString("8.8.8.8")));
    }

    @After
    public void removeTestAppFromDatabase() {
        applicationRepository.delete(testAppId);
    }

    private Map<String, Object> testOxidizedDefaultConfigurationInputModel() {
        Map<String, Object> model = new HashMap<>();
        model.put("oxidizedUsername", "user123");
        model.put("oxidizedPassword", "pass123");
        List<Map> routers = new ArrayList<>();
        Map<String, String> router1 = new HashMap<>();
        router1.put("ipAddress", "7.7.7.7");
        routers.add(router1);
        Map<String, String> router2 = new HashMap<>();
        router2.put("ipAddress", "8.8.8.8");
        routers.add(router2);
        model.put("targets", routers);
        return model;
    }

}
