package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.api.configuration.ConfigurationView;
import net.geant.nmaas.portal.exceptions.ConfigurationNotFoundException;
import net.geant.nmaas.portal.exceptions.OnlyOneConfigurationSupportedException;
import net.geant.nmaas.portal.persistent.entity.Configuration;
import net.geant.nmaas.portal.persistent.entity.Internationalization;
import net.geant.nmaas.portal.persistent.repositories.ConfigurationRepository;
import net.geant.nmaas.portal.persistent.repositories.InternationalizationRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConfigurationManagerTest {

    private ConfigurationRepository repository = mock(ConfigurationRepository.class);

    private ModelMapper modelMapper = new ModelMapper();

    private InternationalizationRepository internationalizationRepository = mock(InternationalizationRepository.class);

    private ConfigurationManager configurationManager;

    private Configuration config;

    ConfigurationView configView;

    private Internationalization internationalization;

    @BeforeEach
    public void setup(){
        this.configurationManager = new ConfigurationManagerImpl(repository, modelMapper, internationalizationRepository);
        this.config = new Configuration(1L, false, false, "en", false);
        this.internationalization = new Internationalization(1L, "pl", true, "Content");
        this.configView = new ConfigurationView(1L, false, false, "pl", false);
    }

    @Test
    public void shouldGetConfiguration(){
        when(repository.count()).thenReturn(1L);
        when(repository.findAll()).thenReturn(Collections.singletonList(config));
        ConfigurationView configView = this.configurationManager.getConfiguration();
        assertEquals(config.isMaintenance(), configView.isMaintenance());
        assertEquals(config.isSsoLoginAllowed(), configView.isSsoLoginAllowed());
        assertEquals(config.getDefaultLanguage(), configView.getDefaultLanguage());
    }

    @Test
    public void shouldAddConfiguration(){
        when(repository.count()).thenReturn(0L);
        Long id = configurationManager.addConfiguration(modelMapper.map(config, ConfigurationView.class));
        assertEquals(config.getId(), id);
        verify(repository, times(1)).save(any());
    }

    @Test
    public void shouldNotAddMultipleConfig(){
        assertThrows(OnlyOneConfigurationSupportedException.class, () -> {
            when(repository.count()).thenReturn(1L);
            configurationManager.addConfiguration(modelMapper.map(config, ConfigurationView.class));
        });
    }

    @Test
    public void shouldUpdateConfiguration(){
        when(repository.findById(config.getId())).thenReturn(Optional.of(config));
        when(internationalizationRepository.findByLanguageOrderByIdDesc(configView.getDefaultLanguage()))
                .thenReturn(Optional.of(internationalization));
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
                    .thenReturn(Optional.of(internationalization));
            configurationManager.updateConfiguration(1L, configView);
        });
    }
}
