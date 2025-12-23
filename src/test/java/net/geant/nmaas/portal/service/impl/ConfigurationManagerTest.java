package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.nmservice.deployment.bulks.BulkDeploymentJob;
import net.geant.nmaas.portal.api.configuration.model.ConfigurationView;
import net.geant.nmaas.portal.api.i18n.api.InternationalizationView;
import net.geant.nmaas.portal.domain.converters.ConfigurationConverter;
import net.geant.nmaas.portal.exceptions.ConfigurationNotFoundException;
import net.geant.nmaas.portal.exceptions.OnlyOneConfigurationSupportedException;
import net.geant.nmaas.portal.persistence.entity.Configuration;
import net.geant.nmaas.portal.persistence.repositories.ConfigurationRepository;
import net.geant.nmaas.portal.persistence.repositories.DomainRepository;
import net.geant.nmaas.portal.persistence.repositories.InternationalizationSimpleRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.scheduling.ScheduleManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConfigurationManagerTest {

    private final ConfigurationRepository repository = mock(ConfigurationRepository.class);
    private final InternationalizationSimpleRepository internationalizationRepository = mock(InternationalizationSimpleRepository.class);
    private final ModelMapper modelMapper = new ModelMapper();
    private final ScheduleManager scheduleManager = mock(ScheduleManager.class);
    private final BulkDeploymentJob bulkDeploymentJob = mock(BulkDeploymentJob.class);
    private final DomainRepository domainRepository = mock(DomainRepository.class);

    private ConfigurationManager configurationManager;

    private Configuration config;
    private ConfigurationView configView;
    private InternationalizationView internationalization;

    @BeforeEach
    public void setup() {
        this.configurationManager = new ConfigurationManagerImpl(
                repository, internationalizationRepository, scheduleManager, bulkDeploymentJob, domainRepository, modelMapper);
        this.config = Configuration.builder()
                .id(1L)
                .maintenance(true)
                .ssoLoginAllowed(true)
                .defaultLanguage("en")
                .testInstance(true)
                .sendAppInstanceFailureEmails(true)
                .appInstanceFailureEmails("")
                .registrationDomainSelectionEnabled(true)
                .bulkDomainsAllowForSsoAccounts(false)
                .bulkDomainsSendEmailForNewAccounts(false)
                .build();
        this.internationalization = new InternationalizationView("pl", true, "{\"test\":\"test\"}");
        this.configView = new ConfigurationView(1L, false, false, "pl",
                false, false, new ArrayList<>(), true, true, false, "0 */1 * * * ?", 2, 60, 10, "", "0 */1 * * * ?", null);
        this.modelMapper.addConverter(new ConfigurationConverter());
    }

    @Test
    void shouldGetConfiguration() {
        when(repository.count()).thenReturn(1L);
        when(repository.findAll()).thenReturn(Collections.singletonList(config));

        ConfigurationView view = this.configurationManager.getConfiguration();

        assertEquals(config.isMaintenance(), view.isMaintenance());
        assertEquals(config.isSsoLoginAllowed(), view.isSsoLoginAllowed());
        assertEquals(config.getDefaultLanguage(), view.getDefaultLanguage());
        assertEquals(config.isRegistrationDomainSelectionEnabled(), view.isRegistrationDomainSelectionEnabled());
    }

    @Test
    void shouldSetConfiguration() {
        when(repository.count()).thenReturn(0L);
        Long id = configurationManager.setConfiguration(modelMapper.map(config, ConfigurationView.class));
        assertEquals(config.getId(), id);
        verify(repository, times(1)).save(any());
    }

    @Test
    void shouldNotSetConfigIfAlreadyExists() {
        when(repository.count()).thenReturn(1L);
        assertThrows(OnlyOneConfigurationSupportedException.class, () -> {
            configurationManager.setConfiguration(modelMapper.map(config, ConfigurationView.class));
        });
    }

    @Test
    void shouldUpdateConfiguration() {
        when(repository.findById(config.getId())).thenReturn(Optional.of(config));
        when(internationalizationRepository.findByLanguageOrderByIdDesc(configView.getDefaultLanguage()))
                .thenReturn(Optional.of(internationalization.getAsInternationalizationSimple()));
        configurationManager.updateConfiguration(1L, configView);
        verify(repository, times(1)).save(any());
    }

    @Test
    void shouldNotUpdateNotExistingConfig() {
        when(repository.findById(config.getId())).thenReturn(Optional.empty());
        assertThrows(ConfigurationNotFoundException.class, () -> {
            configurationManager.updateConfiguration(1L, configView);
        });
    }

    @Test
    void shouldNotSetNotExistingLanguageAsDefault() {
        when(repository.findById(config.getId())).thenReturn(Optional.of(config));
        when(internationalizationRepository.findByLanguageOrderByIdDesc(configView.getDefaultLanguage()))
                .thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> {
            configurationManager.updateConfiguration(1L, configView);
        });
    }

    @Test
    void shouldNotSetDisabledLanguageAsDefault() {
        internationalization.setEnabled(false);
        when(repository.findById(config.getId())).thenReturn(Optional.of(config));
        when(internationalizationRepository.findByLanguageOrderByIdDesc(configView.getDefaultLanguage()))
                .thenReturn(Optional.of(internationalization.getAsInternationalizationSimple()));
        assertThrows(IllegalStateException.class, () -> {
            configurationManager.updateConfiguration(1L, configView);
        });
    }

}
