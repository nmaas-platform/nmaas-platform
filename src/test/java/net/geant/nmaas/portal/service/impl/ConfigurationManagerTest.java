package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.api.configuration.ConfigurationView;
import net.geant.nmaas.portal.api.i18n.api.InternationalizationView;
import net.geant.nmaas.portal.exceptions.ConfigurationNotFoundException;
import net.geant.nmaas.portal.exceptions.OnlyOneConfigurationSupportedException;
import net.geant.nmaas.portal.persistent.entity.Configuration;
import net.geant.nmaas.portal.persistent.repositories.ConfigurationRepository;
import net.geant.nmaas.portal.persistent.repositories.InternationalizationSimpleRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
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

public class ConfigurationManagerTest {

    private ConfigurationRepository repository = mock(ConfigurationRepository.class);

    private ModelMapper modelMapper = new ModelMapper();

    private InternationalizationSimpleRepository internationalizationRepository = mock(InternationalizationSimpleRepository.class);

    private ConfigurationManager configurationManager;

    private Configuration config;

    ConfigurationView configView;

    private InternationalizationView internationalization;

    @BeforeEach
    public void setup(){
        this.configurationManager = new ConfigurationManagerImpl(repository, modelMapper, internationalizationRepository);
        this.config = new Configuration(1L, false, false, "en", false, false, "", true);
        this.internationalization = new InternationalizationView("pl", true, "{\"test\":\"test\"}");
        this.configView = new ConfigurationView(1L, false, false, "pl", false, false, new ArrayList<>(), true);
    }

    @Test
    public void shouldGetConfiguration(){
        when(repository.count()).thenReturn(1L);
        when(repository.findAll()).thenReturn(Collections.singletonList(config));
        ConfigurationView configView = this.configurationManager.getConfiguration();
        assertEquals(config.isMaintenance(), configView.isMaintenance());
        assertEquals(config.isSsoLoginAllowed(), configView.isSsoLoginAllowed());
        assertEquals(config.getDefaultLanguage(), configView.getDefaultLanguage());
        assertEquals(config.isRegistrationDomainSelectionEnabled(), configView.isRegistrationDomainSelectionEnabled());
    }

    @Test
    public void shouldSetConfiguration(){
        when(repository.count()).thenReturn(0L);
        Long id = configurationManager.setConfiguration(modelMapper.map(config, ConfigurationView.class));
        assertEquals(config.getId(), id);
        verify(repository, times(1)).save(any());
    }

    @Test
    public void shouldNotSetConfigIfAlreadyExists(){
        assertThrows(OnlyOneConfigurationSupportedException.class, () -> {
            when(repository.count()).thenReturn(1L);
            configurationManager.setConfiguration(modelMapper.map(config, ConfigurationView.class));
        });
    }

    @Test
    public void shouldUpdateConfiguration(){
        when(repository.findById(config.getId())).thenReturn(Optional.of(config));
        when(internationalizationRepository.findByLanguageOrderByIdDesc(configView.getDefaultLanguage()))
                .thenReturn(Optional.of(internationalization.getAsInternationalizationSimple()));
        configurationManager.updateConfiguration(1L, configView);
        verify(repository, times(1)).save(any());
    }

    @Test
    public void shouldNotUpdateNotExistingConfig(){
        assertThrows(ConfigurationNotFoundException.class, () -> {
            when(repository.findById(config.getId())).thenReturn(Optional.empty());
            configurationManager.updateConfiguration(1L, configView);
        });
    }

    @Test
    public void shouldNotSetNotExistingLanguageAsDefault(){
        assertThrows(IllegalArgumentException.class, () -> {
            when(repository.findById(config.getId())).thenReturn(Optional.of(config));
            when(internationalizationRepository.findByLanguageOrderByIdDesc(configView.getDefaultLanguage()))
                    .thenReturn(Optional.empty());
            configurationManager.updateConfiguration(1L, configView);
        });
    }

    @Test
    public void shouldNotSetDisabledLanguageAsDefault(){
        assertThrows(IllegalStateException.class, () -> {
            this.internationalization.setEnabled(false);
            when(repository.findById(config.getId())).thenReturn(Optional.of(config));
            when(internationalizationRepository.findByLanguageOrderByIdDesc(configView.getDefaultLanguage()))
                    .thenReturn(Optional.of(internationalization.getAsInternationalizationSimple()));
            configurationManager.updateConfiguration(1L, configView);
        });
    }
}
