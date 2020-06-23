package net.geant.nmaas.nmservice.configuration;

import freemarker.template.Template;
import net.geant.nmaas.nmservice.configuration.entities.ConfigFileTemplate;
import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfiguration;
import net.geant.nmaas.nmservice.configuration.repositories.ConfigFileTemplatesRepository;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api.KubernetesChartView;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api.KubernetesTemplateView;
import net.geant.nmaas.portal.api.domain.AppConfigurationSpecView;
import net.geant.nmaas.portal.api.domain.AppDeploymentSpecView;
import net.geant.nmaas.portal.api.domain.AppDescriptionView;
import net.geant.nmaas.portal.api.domain.ApplicationView;
import net.geant.nmaas.portal.api.domain.ConfigFileTemplateView;
import net.geant.nmaas.portal.api.domain.ConfigWizardTemplateView;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import net.geant.nmaas.portal.service.ApplicationService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class OxidizedConfigFileTemplatesTest {

    private static final String TEST_CONFIG_ID_1 = "1";
    private static final String TEST_CONFIG_ID_2 = "2";
    private static final String TEST_TEMPLATE_NAME_1 = "config";
    private static final String TEST_TEMPLATE_NAME_2 = "router.db";

    @Autowired
    private NmServiceConfigurationFilePreparer configurationsPreparer;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ConfigFileTemplatesRepository configFileTemplatesRepository;

    private Long oxidizedAppId;

    @BeforeEach
    public void setup() {
        ApplicationView app = getDefaultAppView();
        ConfigFileTemplateView oxidizedConfigTemplate1 = new ConfigFileTemplateView();
        oxidizedConfigTemplate1.setConfigFileName("config");
        oxidizedConfigTemplate1.setApplicationId(oxidizedAppId);
        oxidizedConfigTemplate1.setConfigFileTemplateContent("---\\nusername: ${oxidizedUsername}\\npassword: ${oxidizedPassword}\\nmodel: junos\\ninterval: 600\\nuse_syslog: false\\ndebug: false\\nthreads: 30\\ntimeout: 20\\nretries: 3\\nprompt: !ruby/regexp /^([\\w.@-]+[#>]\\s?)$/\\nrest: 0.0.0.0:8888\\nvars: {}\\ngroups: {}\\npid: \\\"/root/.config/oxidized/pid\\\"\\ninput:\\n  default: ssh, telnet\\n  debug: false\\n  ssh:\\n    secure: false\\noutput:\\n  default: git\\n  file:\\n    directory: \\\"/root/.config/oxidized/configs\\\"\\n  git:\\n    user: oxidized\\n    email: oxidized@man.poznan.pl\\n    repo: \\\"/root/.config/oxidized/oxidized.git\\\"\\nsource:\\n  default: csv\\n  csv:\\n    file: \\\"/root/.config/oxidized/router.db\\\"\\n    delimiter: !ruby/regexp /:/\\n    map:\\n      name: 0\\n      model: 1\\nmodel_map:\\n  cisco: ios\\n  juniper: junos");
        ConfigFileTemplateView oxidizedConfigTemplate2 = new ConfigFileTemplateView();
        oxidizedConfigTemplate2.setConfigFileName("router.db");
        oxidizedConfigTemplate2.setApplicationId(oxidizedAppId);
        oxidizedConfigTemplate2.setConfigFileTemplateContent("<#list targets as target>\\n${target.ipAddress}:junos\\n</#list>");
        app.getAppConfigurationSpec().setTemplates(Arrays.asList(oxidizedConfigTemplate1, oxidizedConfigTemplate2));
        oxidizedAppId = applicationService.create(app, "admin").getId();
    }

    @Test
    public void shouldBuildConfigFromTemplateAndUserProvidedInput() throws Exception {
        List<ConfigFileTemplate> configFileTemplates = configFileTemplatesRepository.getAllByApplicationId(oxidizedAppId);
        Optional<ConfigFileTemplate> nmServiceConfigurationTemplate =
                configFileTemplates.stream().filter(t -> t.getConfigFileName().endsWith(TEST_TEMPLATE_NAME_1)).findFirst();
        Template template = configurationsPreparer.convertToTemplate(nmServiceConfigurationTemplate.orElseThrow(Exception::new));
        NmServiceConfiguration nmServiceConfiguration =
                configurationsPreparer.buildConfigFromTemplateAndUserProvidedInput(
                        TEST_CONFIG_ID_1,
                        template,
                        testOxidizedDefaultConfigurationInputModel());
        assertThat(nmServiceConfiguration.getConfigFileName(), equalTo("config"));
        assertThat(nmServiceConfiguration.getConfigFileContent(),
                Matchers.allOf(containsString("user123"), containsString("pass123")));
        nmServiceConfigurationTemplate =
                configFileTemplates.stream().filter(t -> t.getConfigFileName().endsWith(TEST_TEMPLATE_NAME_2)).findFirst();
        template = configurationsPreparer.convertToTemplate(nmServiceConfigurationTemplate.orElseThrow(Exception::new));
        nmServiceConfiguration =
                configurationsPreparer.buildConfigFromTemplateAndUserProvidedInput(
                        TEST_CONFIG_ID_2,
                        template,
                        testOxidizedDefaultConfigurationInputModel());
        assertThat(nmServiceConfiguration.getConfigFileName(), equalTo("router.db"));
        assertThat(nmServiceConfiguration.getConfigFileContent(),
                Matchers.allOf(containsString("7.7.7.7"), containsString("8.8.8.8")));
    }

    @AfterEach
    public void removeTestAppFromDatabase() {
        applicationRepository.deleteAll();
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

    private ApplicationView getDefaultAppView(){
        ApplicationView applicationView = new ApplicationView();
        applicationView.setName("test");
        applicationView.setVersion("testversion");
        applicationView.setOwner("owner");
        applicationView.setDescriptions(Collections.singletonList(new AppDescriptionView("en", "test", "testfull")));
        AppDeploymentSpecView appDeploymentSpec = new AppDeploymentSpecView();
        appDeploymentSpec.setKubernetesTemplate(new KubernetesTemplateView(1L,
                new KubernetesChartView(1L, "name", "version"),
                "archive",
                null));
        applicationView.setAppDeploymentSpec(appDeploymentSpec);
        applicationView.setConfigWizardTemplate(new ConfigWizardTemplateView(1L, "template"));
        applicationView.setAppConfigurationSpec(new AppConfigurationSpecView());
        applicationView.getAppConfigurationSpec().setConfigFileRepositoryRequired(false);
        applicationView.setState(ApplicationState.ACTIVE);
        return applicationView;
    }

}
