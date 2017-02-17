package net.geant.nmaas.nmservice.configuration;

import freemarker.template.Template;
import net.geant.nmaas.nmservice.configuration.exceptions.NmServiceConfigurationFailedException;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static net.geant.nmaas.nmservice.configuration.NmServiceConfigurationTemplatesRepository.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class NmServiceConfigurationTemplatesTest {

    @Autowired
    private NmServiceConfigurationTemplatesRepository templatesRepository;

    @Autowired
    private SimpleNmServiceConfigurationExecutor configurationExecutor;

    @Test
    public void shouldPopulateAndPrintConfigurationFile() throws Exception {
        List<Template> templates = templatesRepository.loadTemplates(AppLifecycleManager.OXIDIZED_APPLICATION_ID);
        assertThat(templates.size(), equalTo(2));
        assertThat(templates.get(0).getName(), endsWith(DEFAULT_TEMPLATE_FILE_NAME_SUFFIX));
        assertThat(configurationExecutor.configFileNameFromTemplateName(templates.get(0).getName()),
                not(containsString(DEFAULT_TEMPLATE_FILE_NAME_SUFFIX)));
        System.out.println(configurationExecutor.configFileNameFromTemplateName(templates.get(0).getName()));
    }

}
