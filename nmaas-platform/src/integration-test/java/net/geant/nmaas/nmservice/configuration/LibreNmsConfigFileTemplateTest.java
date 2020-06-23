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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class LibreNmsConfigFileTemplateTest {

    private static final String TEST_CONFIG_ID_1 = "1";
    private static final String TEST_TEMPLATE_NAME = "addhosts.cfg";

    @Autowired
    private NmServiceConfigurationFilePreparer configurationsPreparer;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ConfigFileTemplatesRepository configFileTemplatesRepository;

    private Long librenmsAppId;

    @BeforeEach
    public void setup() {
        ApplicationView app = getDefaultAppView();
        ConfigFileTemplateView librenmsConfigTemplate1 = new ConfigFileTemplateView();
        librenmsConfigTemplate1.setConfigFileName(TEST_TEMPLATE_NAME);
        librenmsConfigTemplate1.setConfigFileTemplateContent("<#list targets as target>\\n-f ${target.ipAddress} ${target.snmpCommunity} ${target.snmpVersion}\\n</#list>");
        app.getAppConfigurationSpec().setTemplates(Collections.singletonList(librenmsConfigTemplate1));
        librenmsAppId = applicationService.create(app,"admin").getId();
    }

    @Test
    public void shouldBuildConfigFromTemplateAndUserProvidedInput() {
        List<ConfigFileTemplate> configFileTemplates = configFileTemplatesRepository.getAllByApplicationId(librenmsAppId);
        Template template = configurationsPreparer.convertToTemplate(configFileTemplates.get(0));
        NmServiceConfiguration nmServiceConfiguration =
                configurationsPreparer.buildConfigFromTemplateAndUserProvidedInput(
                        TEST_CONFIG_ID_1,
                        template,
                        testLibreNmsDefaultConfigurationInputModel());
        assertThat(nmServiceConfiguration.getConfigFileName(), equalTo(TEST_TEMPLATE_NAME));
        assertThat(nmServiceConfiguration.getConfigFileContent(),
                Matchers.allOf(containsString("192.168.1.1"), containsString("v2c"), containsString("private")));
    }

    @AfterEach
    public void removeTestAppFromDatabase() {
        applicationRepository.deleteAll();
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
