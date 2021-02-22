package net.geant.nmaas.nmservice.configuration;

import freemarker.template.Template;
import net.geant.nmaas.nmservice.configuration.entities.ConfigFileTemplate;
import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfiguration;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.geant.nmaas.nmservice.configuration.ConfigFilePreparerHelper.convertToFreemarkerTemplate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ConfigFilePreparerTest {

    private ConfigFilePreparer configFilePreparer;

    @BeforeEach
    public void init() {
        configFilePreparer = new ConfigFilePreparer();
    }

    @Test
    public void shouldBuildConfigFromElkTemplate() {
        assertDoesNotThrow(() -> {
            ConfigFileTemplate elkConfigTemplate1 = new ConfigFileTemplate();
            elkConfigTemplate1.setConfigFileName("kibana.yml");
            elkConfigTemplate1.setConfigFileTemplateContent("server.name: kibana\nserver.host: \"0\"\nelasticsearch.hosts: [ \"http://elasticsearch:9200\" ]\nmonitoring.ui.container.elasticsearch.enabled: true\nelasticsearch.username: \"kibana\"\nelasticsearch.password: \"pristap\"  \nxpack.security.encryptionKey: \"${helper.randomString(32)}\"\nxpack.encryptedSavedObjects.encryptionKey: \"${helper.randomString(32)}\"");
            Template template = convertToFreemarkerTemplate(elkConfigTemplate1);
            Map<String, Object> appConfigurationModel = new HashMap<>();
            appConfigurationModel.put("helper", new ConfigFilePreparerHelper());
            String fileContent = configFilePreparer.buildConfigFromTemplateAndUserProvidedInput(
                    "1",
                    "kibana.yml",
                    "kibana",
                    template,
                    appConfigurationModel).getConfigFileContent();
            assertNotNull(fileContent);
            System.out.println(fileContent);
        });
    }

    @Test
    public void shouldBuildConfigFromLibrenmsTemplate() {
        assertDoesNotThrow(() -> {
            ConfigFileTemplate librenmsConfigTemplate1 = new ConfigFileTemplate();
            librenmsConfigTemplate1.setConfigFileName("addhosts.cfg");
            librenmsConfigTemplate1.setConfigFileTemplateContent("<#list targets as target>\\n-f ${target.ipAddress} ${target.snmpCommunity} ${target.snmpVersion}\\n</#list>");
            Template template = convertToFreemarkerTemplate(librenmsConfigTemplate1);
            NmServiceConfiguration nmServiceConfiguration =
                    configFilePreparer.buildConfigFromTemplateAndUserProvidedInput(
                            "2",
                            "addhosts.cfg",
                            null,
                            template,
                            testLibreNmsDefaultConfigurationInputModel());
            assertThat(nmServiceConfiguration.getConfigFileContent(),
                    Matchers.allOf(containsString("192.168.1.1"), containsString("v2c"), containsString("private")));
        });
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

    @Test
    public void shouldBuildConfigFromOxidizedTemplate() {
        assertDoesNotThrow(() -> {
            ConfigFileTemplate oxidizedConfigTemplate1 = new ConfigFileTemplate();
            oxidizedConfigTemplate1.setConfigFileName("config");
            oxidizedConfigTemplate1.setConfigFileTemplateContent("---\\nusername: ${oxidizedUsername}\\npassword: ${oxidizedPassword}\\nmodel: junos\\ninterval: 600\\nuse_syslog: false\\ndebug: false\\nthreads: 30\\ntimeout: 20\\nretries: 3\\nprompt: !ruby/regexp /^([\\w.@-]+[#>]\\s?)$/\\nrest: 0.0.0.0:8888\\nvars: {}\\ngroups: {}\\npid: \\\"/root/.config/oxidized/pid\\\"\\ninput:\\n  default: ssh, telnet\\n  debug: false\\n  ssh:\\n    secure: false\\noutput:\\n  default: git\\n  file:\\n    directory: \\\"/root/.config/oxidized/configs\\\"\\n  git:\\n    user: oxidized\\n    email: oxidized@man.poznan.pl\\n    repo: \\\"/root/.config/oxidized/oxidized.git\\\"\\nsource:\\n  default: csv\\n  csv:\\n    file: \\\"/root/.config/oxidized/router.db\\\"\\n    delimiter: !ruby/regexp /:/\\n    map:\\n      name: 0\\n      model: 1\\nmodel_map:\\n  cisco: ios\\n  juniper: junos");
            ConfigFileTemplate oxidizedConfigTemplate2 = new ConfigFileTemplate();
            oxidizedConfigTemplate2.setConfigFileName("router.db");
            oxidizedConfigTemplate2.setConfigFileTemplateContent("<#list targets as target>\\n${target.ipAddress}:junos\\n</#list>");
            Template template = convertToFreemarkerTemplate(oxidizedConfigTemplate1);
            NmServiceConfiguration nmServiceConfiguration =
                    configFilePreparer.buildConfigFromTemplateAndUserProvidedInput(
                            "3",
                            "config",
                            null,
                            template,
                            testOxidizedDefaultConfigurationInputModel());
            assertThat(nmServiceConfiguration.getConfigFileName(), equalTo("config"));
            assertThat(nmServiceConfiguration.getConfigFileContent(),
                    Matchers.allOf(containsString("user123"), containsString("pass123")));
            template = convertToFreemarkerTemplate(oxidizedConfigTemplate2);
            nmServiceConfiguration =
                    configFilePreparer.buildConfigFromTemplateAndUserProvidedInput(
                            "4",
                            "router.db",
                            "config",
                            template,
                            testOxidizedDefaultConfigurationInputModel());
            assertThat(nmServiceConfiguration.getConfigFileName(), equalTo("router.db"));
            assertThat(nmServiceConfiguration.getConfigFileDirectory(), equalTo("config"));
            assertThat(nmServiceConfiguration.getConfigFileContent(),
                    Matchers.allOf(containsString("7.7.7.7"), containsString("8.8.8.8")));
        });
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

    @Test
    public void shouldBuildConfigFromTemplateAndDeploymentParameters() {
        assertDoesNotThrow(() -> {
            ConfigFileTemplate template1 = new ConfigFileTemplate();
            template1.setConfigFileName("config.yml");
            template1.setConfigFileTemplateContent("server.name: \"${RELEASE_NAME}\"");
            Template template = convertToFreemarkerTemplate(template1);
            Map<String, Object> appConfigurationModel = new HashMap<>();
            appConfigurationModel.put("RELEASE_NAME", "release_name");
            String fileContent = configFilePreparer.buildConfigFromTemplateAndUserProvidedInput(
                    "10",
                    "config.yml",
                    null,
                    template,
                    appConfigurationModel).getConfigFileContent();
            assertNotNull(fileContent);
            assertEquals("server.name: \"release_name\"", fileContent);
        });
    }

}
