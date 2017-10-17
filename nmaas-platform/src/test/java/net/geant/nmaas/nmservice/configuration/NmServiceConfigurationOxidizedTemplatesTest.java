package net.geant.nmaas.nmservice.configuration;

import freemarker.template.Template;
import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfiguration;
import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfigurationTemplate;
import net.geant.nmaas.nmservice.configuration.repositories.NmServiceConfigFileTemplatesRepository;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class NmServiceConfigurationOxidizedTemplatesTest {

    private static final String TEST_CONFIG_ID_1 = "1";
    private static final String TEST_CONFIG_ID_2 = "2";
    private static final String TEST_TEMPLATE_NAME_1 = "config";
    private static final String TEST_TEMPLATE_NAME_2 = "router.db";

    @Autowired
    private NmServiceConfigFileTemplatesRepository templatesRepository;

    @Autowired
    private NmServiceConfigurationFilePreparer configurationsPreparer;

    @Autowired
    private ApplicationRepository applicationRepository;

    private Long oxidizedAppId;

    @Before
    public void setup() {
        Application app = new Application("oxidizedAppName");
        app.setVersion("oxidizedAppVersion");
        oxidizedAppId = applicationRepository.save(app).getId();
        NmServiceConfigurationTemplate oxidizedConfigTemplate1 = new NmServiceConfigurationTemplate();
        oxidizedConfigTemplate1.setApplicationId(oxidizedAppId);
        oxidizedConfigTemplate1.setConfigFileName("config");
        oxidizedConfigTemplate1.setConfigFileTemplateContent("---\\nusername: ${oxidizedUsername}\\npassword: ${oxidizedPassword}\\nmodel: junos\\ninterval: 600\\nuse_syslog: false\\ndebug: false\\nthreads: 30\\ntimeout: 20\\nretries: 3\\nprompt: !ruby/regexp /^([\\w.@-]+[#>]\\s?)$/\\nrest: 0.0.0.0:8888\\nvars: {}\\ngroups: {}\\npid: \\\"/root/.config/oxidized/pid\\\"\\ninput:\\n  default: ssh, telnet\\n  debug: false\\n  ssh:\\n    secure: false\\noutput:\\n  default: git\\n  file:\\n    directory: \\\"/root/.config/oxidized/configs\\\"\\n  git:\\n    user: oxidized\\n    email: oxidized@man.poznan.pl\\n    repo: \\\"/root/.config/oxidized/oxidized.git\\\"\\nsource:\\n  default: csv\\n  csv:\\n    file: \\\"/root/.config/oxidized/router.db\\\"\\n    delimiter: !ruby/regexp /:/\\n    map:\\n      name: 0\\n      model: 1\\nmodel_map:\\n  cisco: ios\\n  juniper: junos");
        templatesRepository.save(oxidizedConfigTemplate1);
        NmServiceConfigurationTemplate oxidizedConfigTemplate2 = new NmServiceConfigurationTemplate();
        oxidizedConfigTemplate2.setApplicationId(oxidizedAppId);
        oxidizedConfigTemplate2.setConfigFileName("router.db");
        oxidizedConfigTemplate2.setConfigFileTemplateContent("<#list targets as target>\\n${target.ipAddress}:junos\\n</#list>");
        templatesRepository.save(oxidizedConfigTemplate2);
    }

    @Test
    public void shouldBuildConfigFromTemplateAndUserProvidedInput() throws Exception {
        List<NmServiceConfigurationTemplate> nmServiceConfigurationTemplates =
                templatesRepository.findAllByApplicationId(oxidizedAppId);
        Optional<NmServiceConfigurationTemplate> nmServiceConfigurationTemplate =
                nmServiceConfigurationTemplates.stream().filter(t -> t.getConfigFileName().endsWith(TEST_TEMPLATE_NAME_1)).findFirst();
        Template template = configurationsPreparer.convertToTemplate(nmServiceConfigurationTemplate.orElseThrow(() -> new Exception()));
        NmServiceConfiguration nmServiceConfiguration =
                configurationsPreparer.buildConfigFromTemplateAndUserProvidedInput(
                        TEST_CONFIG_ID_1,
                        template,
                        testOxidizedDefaultConfigurationInputModel());
        assertThat(nmServiceConfiguration.getConfigFileName(), equalTo("config"));
        assertThat(nmServiceConfiguration.getConfigFileContent(),
                Matchers.allOf(containsString("user123"), containsString("pass123")));
        nmServiceConfigurationTemplate =
                nmServiceConfigurationTemplates.stream().filter(t -> t.getConfigFileName().endsWith(TEST_TEMPLATE_NAME_2)).findFirst();
        template = configurationsPreparer.convertToTemplate(nmServiceConfigurationTemplate.orElseThrow(() -> new Exception()));
        nmServiceConfiguration =
                configurationsPreparer.buildConfigFromTemplateAndUserProvidedInput(
                        TEST_CONFIG_ID_2,
                        template,
                        testOxidizedDefaultConfigurationInputModel());
        assertThat(nmServiceConfiguration.getConfigFileName(), equalTo("router.db"));
        assertThat(nmServiceConfiguration.getConfigFileContent(),
                Matchers.allOf(containsString("7.7.7.7"), containsString("8.8.8.8")));
    }

    @After
    public void removeTestAppFromDatabase() {
        applicationRepository.deleteAll();
        templatesRepository.deleteAll();
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
