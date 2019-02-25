package net.geant.nmaas.portal.api.i18n;

import net.geant.nmaas.portal.api.configuration.ConfigurationView;
import net.geant.nmaas.portal.api.i18n.api.LanguageView;
import net.geant.nmaas.portal.persistent.entity.Internationalization;
import net.geant.nmaas.portal.persistent.repositories.InternationalizationRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InternationalizationControllerTest {

    private InternationalizationRepository repository = mock(InternationalizationRepository.class);

    private ConfigurationManager configurationManager = mock(ConfigurationManager.class);

    private ModelMapper modelMapper = new ModelMapper();

    private InternationalizationController internationalizationController;

    private LanguageView language;

    @BeforeEach
    public void setup(){
        this.internationalizationController = new InternationalizationController(repository, configurationManager, modelMapper);
        this.language = new LanguageView("pl", true);
    }

    @Test
    public void shouldSaveLanguageContent(){
        internationalizationController.saveLanguageContent("pl", "Test content");
        verify(repository, times(1)).save(any());
    }

    @Test
    public void shouldGetAllSupportedLanguages(){
        when(repository.findAll()).thenReturn(Collections.singletonList(new Internationalization(1L, "pl", true, "Test content")));
        List<LanguageView> languageList = internationalizationController.getAllSupportedLanguages();
        assertEquals(1, languageList.size());
        assertEquals("pl", languageList.get(0).getLanguage());
        assertTrue(languageList.get(0).isEnabled());
    }

    @Test
    public void shouldReturnEmptyList(){
        when(repository.findAll()).thenReturn(Collections.emptyList());
        List<LanguageView> languageList = internationalizationController.getAllSupportedLanguages();
        assertTrue(languageList.isEmpty());
    }

    @Test
    public void shouldChangeLanguageState(){
        when(configurationManager.getConfiguration()).thenReturn(new ConfigurationView(false, false, "fr"));
        Internationalization internationalization = new Internationalization(1L, "pl", false, "Test content");
        when(repository.findByLanguageOrderByIdDesc(language.getLanguage())).thenReturn(Optional.of(internationalization));
        internationalizationController.changeSupportedLanguageState(language);
        verify(repository, times(1)).save(any());
    }

    @Test
    public void shouldThrowAnExceptionWhenLanguageIsNotFound(){
        assertThrows(IllegalArgumentException.class, () -> {
            when(repository.findByLanguageOrderByIdDesc(language.getLanguage())).thenReturn(Optional.empty());
            internationalizationController.changeSupportedLanguageState(language);
        });
    }

    @Test
    public void shouldThrowAnExceptionWhenDisablingDefaultLanguage(){
        assertThrows(IllegalStateException.class, () -> {
            Internationalization internationalization = new Internationalization(1L, "pl", false, "Test content");
            when(repository.findByLanguageOrderByIdDesc(language.getLanguage())).thenReturn(Optional.of(internationalization));
            when(configurationManager.getConfiguration()).thenReturn(new ConfigurationView(false, false, "pl"));
            internationalizationController.changeSupportedLanguageState(language);
        });
    }
}
