package net.geant.nmaas.portal.persistence.entity;

import net.geant.nmaas.portal.api.configuration.model.ConfigurationDto;
import net.geant.nmaas.portal.domain.converters.ConfigurationConverter;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConfigurationTest {

    @Test
    void shouldCreateConfigurationWithEmailList() {
        List<String> emails = Lists.newArrayList("admin@email.com", "user@email.com");
        Configuration configuration = Configuration.builder()
                .maintenance(true)
                .ssoLoginAllowed(true)
                .defaultLanguage("en")
                .testInstance(true)
                .sendAppInstanceFailureEmails(true)
                .appInstanceFailureEmails(String.join(";", emails))
                .registrationDomainSelectionEnabled(true)
                .bulkDomainsAllowForSsoAccounts(false)
                .bulkDomainsSendEmailForNewAccounts(false)
                .build();
        assertEquals(2, configuration.getAppInstanceFailureEmailList().size());
    }

    @Test
    void bareModelMapperShouldMapBetweenConfigurationAndConfigurationDto() {
        ModelMapper mm = new ModelMapper();
        mm.addConverter(new ConfigurationConverter());
        List<String> emails = Lists.newArrayList("admin@email.com", "user@email.com");
        Configuration configuration = Configuration.builder()
                .maintenance(true)
                .ssoLoginAllowed(true)
                .defaultLanguage("en")
                .testInstance(true)
                .sendAppInstanceFailureEmails(true)
                .appInstanceFailureEmails(String.join(";", emails))
                .registrationDomainSelectionEnabled(true)
                .bulkDomainsAllowForSsoAccounts(false)
                .bulkDomainsSendEmailForNewAccounts(false)
                .defaultDomainForSsoUsers(new Domain(10L))
                .build();

        ConfigurationDto configurationDto = mm.map(configuration, ConfigurationDto.class);
        assertEquals(2, configurationDto.getAppInstanceFailureEmailList().size());
        assertEquals(10L, configurationDto.getDefaultDomainForSsoUsers());

        Configuration conf2 = mm.map(configurationDto, Configuration.class);
        assertEquals(2, conf2.getAppInstanceFailureEmailList().size());
        assertNull(conf2.getDefaultDomainForSsoUsers());
    }

}
