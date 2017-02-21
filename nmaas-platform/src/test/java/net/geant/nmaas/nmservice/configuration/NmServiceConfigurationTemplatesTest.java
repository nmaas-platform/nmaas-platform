package net.geant.nmaas.nmservice.configuration;

import freemarker.template.Template;
import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfiguration;
import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfigurationTemplatesRepository;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.geant.nmaas.nmservice.configuration.repository.NmServiceConfigurationTemplatesRepository.DEFAULT_TEMPLATE_FILE_NAME_SUFFIX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class NmServiceConfigurationTemplatesTest {

    private static final String TEST_CONFIG_ID_1 = "1";

    private static final String TEST_CONFIG_ID_2 = "2";

    @Autowired
    private NmServiceConfigurationTemplatesRepository templatesRepository;

    @Autowired
    private SimpleNmServiceConfigurationExecutor configurationExecutor;

    @Test
    public void shouldPopulateAndPrintConfigurationFile() throws Exception {
        List<Template> templates = templatesRepository.loadTemplates(AppLifecycleManager.OXIDIZED_APPLICATION_ID);
        assertThat(templates.size(), equalTo(2));
        assertThat(templates.get(0).getName(), endsWith(DEFAULT_TEMPLATE_FILE_NAME_SUFFIX));
        assertThat(SimpleNmServiceConfigurationHelper.configFileNameFromTemplateName(templates.get(0).getName()),
                not(containsString(DEFAULT_TEMPLATE_FILE_NAME_SUFFIX)));
    }

    @Test
    public void shouldBuildConfigFromTemplateAndUserProvidedInput() throws Exception {
        List<Template> templates = templatesRepository.loadTemplates(AppLifecycleManager.OXIDIZED_APPLICATION_ID);
        NmServiceConfiguration nmServiceConfiguration =
                configurationExecutor.buildConfigFromTemplateAndUserProvidedInput(TEST_CONFIG_ID_1, templates.get(0), testOxidizedDefaultConfigurationInputModel());
        assertThat(nmServiceConfiguration.getConfigFileName(), equalTo("config"));
        assertThat(new String(nmServiceConfiguration.getConfigFileContent(), "UTF-8"),
                Matchers.allOf(containsString("user123"), containsString("pass123")));
        nmServiceConfiguration =
                configurationExecutor.buildConfigFromTemplateAndUserProvidedInput(TEST_CONFIG_ID_2, templates.get(1), testOxidizedDefaultConfigurationInputModel());
        assertThat(nmServiceConfiguration.getConfigFileName(), equalTo("router.db"));
        assertThat(new String(nmServiceConfiguration.getConfigFileContent(), "UTF-8"),
                Matchers.allOf(containsString("7.7.7.7"), containsString("8.8.8.8")));
    }

    private Map<String, Object> testOxidizedDefaultConfigurationInputModel() {
        Map<String, Object> model = new HashMap<>();
        model.put("oxidizedUsername", "user123");
        model.put("oxidizedPassword", "pass123");
        List<String> routers = new ArrayList<>();
        routers.add("7.7.7.7");
        routers.add("8.8.8.8");
        model.put("routers", routers);
        return model;
    }

}
