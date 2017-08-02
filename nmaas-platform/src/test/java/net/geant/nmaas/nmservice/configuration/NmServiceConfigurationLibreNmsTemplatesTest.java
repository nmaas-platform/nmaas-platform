package net.geant.nmaas.nmservice.configuration;

import freemarker.template.Template;
import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfiguration;
import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfigurationTemplate;
import net.geant.nmaas.nmservice.configuration.repositories.NmServiceConfigurationTemplatesRepository;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class NmServiceConfigurationLibreNmsTemplatesTest {

    private static final String TEST_CONFIG_ID_1 = "1";
    private static final String TEST_TEMPLATE_NAME = "addhosts.cfg";

    @Autowired
    private NmServiceConfigurationTemplatesRepository templatesRepository;

    @Autowired
    private NmServiceConfigurationsPreparer configurationsPreparer;

    @Autowired
    private ApplicationRepository applicationRepository;

    private Long librenmsAppId;

    @Before
    public void setup() {
        Application app = new Application("librenmsAppName");
        app.setVersion("librenmsAppVersion");
        librenmsAppId = applicationRepository.save(app).getId();
        NmServiceConfigurationTemplate librenmsConfigTemplate1 = new NmServiceConfigurationTemplate();
        librenmsConfigTemplate1.setApplicationId(librenmsAppId);
        librenmsConfigTemplate1.setConfigFileName("addhosts.cfg");
        librenmsConfigTemplate1.setConfigFileTemplateContent("<#list targets as target>\\n-f ${target.ipAddress} ${target.snmpCommunity} ${target.snmpVersion}\\n</#list>");
        templatesRepository.save(librenmsConfigTemplate1);
    }

    @Test
    public void shouldLoadLibrenmsTemplatesFromRepository() throws Exception {
        List<NmServiceConfigurationTemplate> templates = templatesRepository.findAllByApplicationId(librenmsAppId);
        assertThat(templates.size(), equalTo(1));
        assertThat(templates.get(0).getConfigFileName(), equalTo(TEST_TEMPLATE_NAME));
    }

    @Test
    public void shouldBuildConfigFromTemplateAndUserProvidedInput() throws Exception {
        List<NmServiceConfigurationTemplate> nmServiceConfigurationTemplates = templatesRepository.findAllByApplicationId(librenmsAppId);
        Template template = configurationsPreparer.convertToTemplate(nmServiceConfigurationTemplates.get(0));
        NmServiceConfiguration nmServiceConfiguration =
                configurationsPreparer.buildConfigFromTemplateAndUserProvidedInput(
                        TEST_CONFIG_ID_1,
                        template,
                        testLibreNmsDefaultConfigurationInputModel());
        assertThat(nmServiceConfiguration.getConfigFileName(), equalTo("addhosts.cfg"));
        assertThat(nmServiceConfiguration.getConfigFileContent(),
                Matchers.allOf(containsString("192.168.1.1"), containsString("v2c"), containsString("private")));
    }

    @After
    public void removeTestAppFromDatabase() {
        applicationRepository.deleteAll();
        templatesRepository.deleteAll();
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
