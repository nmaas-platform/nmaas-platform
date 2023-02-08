package net.geant.nmaas.portal.persistent.entity;

import net.geant.nmaas.portal.api.configuration.ConfigurationView;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigurationTest {

    @Test
    public void shouldCreateConfigurationWithEmailList() {
        List<String> emails = Lists.newArrayList("admin@email.com", "user@email.com");

        Configuration configuration = new Configuration(true, true, "en", true, true, emails, true);

        assertEquals(2, configuration.getAppInstanceFailureEmailList().size());
    }

    @Test
    public void bareModelMapperShouldMapBetweenConfigurationAndConfigurationView() {
        ModelMapper mm = new ModelMapper();

        List<String> emails = Lists.newArrayList("admin@email.com", "user@email.com");

        Configuration configuration = new Configuration(true, true, "en", true, true, emails, true);

        ConfigurationView configurationView = mm.map(configuration, ConfigurationView.class);

        assertEquals(2, configurationView.getAppInstanceFailureEmailList().size());

        Configuration conf2 = mm.map(configurationView, Configuration.class);

        assertEquals(2, conf2.getAppInstanceFailureEmailList().size());
    }
}
