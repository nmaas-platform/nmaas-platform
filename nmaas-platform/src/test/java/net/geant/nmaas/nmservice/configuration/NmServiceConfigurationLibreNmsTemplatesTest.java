package net.geant.nmaas.nmservice.configuration;

import freemarker.template.Template;
import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfiguration;
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
public class NmServiceConfigurationLibreNmsTemplatesTest {

    private static final String TEST_CONFIG_ID_1 = "1";
    private static final String TEST_TEMPLATE_NAME = "addhosts.cfg-template";
    private static final String LIBRENMS_APP_NAME = "LibreNMS";
    private static final String LIBRENMS_APP_VERSION = "1.0";

    @Autowired
    private NmServiceConfigurationTemplatesRepository templatesRepository;

    @Autowired
    private NmServiceConfigurationsPreparer configurationsPreparer;

    @Autowired
    private ApplicationRepository applicationRepository;

    private Long testAppId;

    @Before
    public void setup() {
        Application app = new Application(LIBRENMS_APP_NAME);
        app.setVersion(LIBRENMS_APP_VERSION);
        testAppId = applicationRepository.save(app).getId();
    }

    @Test
    public void shouldPopulateAndPrintConfigurationFile() throws Exception {
        Identifier libreNmsIdentifier = Identifier.newInstance(String.valueOf(applicationRepository.findByName(LIBRENMS_APP_NAME).get(0).getId()));
        List<Template> templates = templatesRepository.loadTemplates(libreNmsIdentifier);
        assertThat(templates.size(), equalTo(1));
        assertThat(templates.get(0).getName(), endsWith(DEFAULT_TEMPLATE_FILE_NAME_SUFFIX));
        assertThat(SimpleNmServiceConfigurationHelper.configFileNameFromTemplateName(templates.get(0).getName()),
                not(containsString(DEFAULT_TEMPLATE_FILE_NAME_SUFFIX)));
    }

    @Test
    public void shouldBuildConfigFromTemplateAndUserProvidedInput() throws Exception {
        Identifier oxidizedIdentifier = Identifier.newInstance(String.valueOf(applicationRepository.findByName(LIBRENMS_APP_NAME).get(0).getId()));
        List<Template> templates = templatesRepository.loadTemplates(oxidizedIdentifier);
        Optional<Template> tut = templates.stream().filter(t -> t.getName().endsWith(TEST_TEMPLATE_NAME)).findFirst();
        NmServiceConfiguration nmServiceConfiguration =
                configurationsPreparer.buildConfigFromTemplateAndUserProvidedInput(
                        TEST_CONFIG_ID_1,
                        tut.orElseThrow(() -> new Exception()),
                        testLibreNmsDefaultConfigurationInputModel());
        assertThat(nmServiceConfiguration.getConfigFileName(), equalTo("addhosts.cfg"));
        assertThat(new String(nmServiceConfiguration.getConfigFileContent(), "UTF-8"),
                Matchers.allOf(containsString("192.168.1.1"), containsString("v2c"), containsString("private")));
    }

    @After
    public void removeTestAppFromDatabase() {
        applicationRepository.delete(testAppId);
    }

    private Map<String, Object> testLibreNmsDefaultConfigurationInputModel() {
        Map<String, Object> model = new HashMap<>();
        List<Map> routers = new ArrayList<>();
        Map<String, String> router1 = new HashMap<>();
        router1.put("ipAddress", "192.168.1.1");
        router1.put("snmpCommunity", "public");
        router1.put("snmpVersion", "v2c");
        routers.add(router1);
        Map<String, String> router2 = new HashMap<>();
        router2.put("ipAddress", "10.10.3.2");
        router2.put("snmpCommunity", "private");
        router2.put("snmpVersion", "v2");
        routers.add(router2);
        model.put("targets", routers);
        return model;
    }

}
