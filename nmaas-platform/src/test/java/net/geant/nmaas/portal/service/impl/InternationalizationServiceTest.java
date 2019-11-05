package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.api.configuration.ConfigurationView;
import net.geant.nmaas.portal.api.i18n.api.InternationalizationBriefView;
import net.geant.nmaas.portal.api.i18n.api.InternationalizationView;
import net.geant.nmaas.portal.persistent.entity.Internationalization;
import net.geant.nmaas.portal.persistent.repositories.InternationalizationRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.InternationalizationService;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
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

public class InternationalizationServiceTest {

    private InternationalizationRepository repository = mock(InternationalizationRepository.class);

    private ConfigurationManager configurationManager = mock(ConfigurationManager.class);

    private ModelMapper modelMapper = new ModelMapper();

    private InternationalizationService internationalizationService;

    private InternationalizationView language;

    @BeforeEach
    public void setup(){
        this.internationalizationService = new InternationalizationServiceImpl(repository, configurationManager, modelMapper);
        this.language = new InternationalizationView("pl", true, "{}");
    }

    @Test
    public void shouldSaveLanguageContent(){
        internationalizationService.addNewLanguage(this.language);
        verify(repository, times(1)).save(any());
    }

    @Test
    public void shouldNotSaveNullRequest(){
        assertThrows(IllegalArgumentException.class, ()-> internationalizationService.addNewLanguage(null));
    }

    @Test
    public void shouldNotSaveWithEmptyLanguageId(){
        assertThrows(IllegalArgumentException.class, ()-> {
            this.language.setLanguage("");
            internationalizationService.addNewLanguage(this.language);
        });
    }

    @Test
    public void shouldNotSaveWithEmptyContent(){
        assertThrows(IllegalArgumentException.class, ()-> {
            this.language.setContent("");
            internationalizationService.addNewLanguage(this.language);
        });
    }

    @Test
    public void shouldNotSaveWithInvalidJsonContent(){
        assertThrows(IllegalArgumentException.class, ()-> {
            this.language.setContent("{invalid]");
            internationalizationService.addNewLanguage(this.language);
        });
    }

    @Test
    public void shouldGetAllSupportedLanguages(){
        when(repository.findAll()).thenReturn(Collections.singletonList(new Internationalization(1L, "pl", true, "{\"test\":\"content\"")));
        List<InternationalizationBriefView> languageList = internationalizationService.getAllSupportedLanguages();
        assertEquals(1, languageList.size());
        assertEquals("pl", languageList.get(0).getLanguage());
        assertTrue(languageList.get(0).isEnabled());
    }

    @Test
    public void shouldReturnEmptyList(){
        when(repository.findAll()).thenReturn(Collections.emptyList());
        List<InternationalizationBriefView> languageList = internationalizationService.getAllSupportedLanguages();
        assertTrue(languageList.isEmpty());
    }

    @Test
    public void shouldChangeLanguageState(){
        when(configurationManager.getConfiguration()).thenReturn(new ConfigurationView(false, false, "fr", false));
        Internationalization internationalization = new Internationalization(1L, "pl", false, "{\"test\":\"content\"");
        when(repository.findByLanguageOrderByIdDesc(language.getLanguage())).thenReturn(Optional.of(internationalization));
        internationalizationService.changeLanguageState(language);
        verify(repository, times(1)).save(any());
    }

    @Test
    public void shouldThrowAnExceptionWhenLanguageIsNotFound(){
        assertThrows(IllegalArgumentException.class, () -> {
            when(repository.findByLanguageOrderByIdDesc(language.getLanguage())).thenReturn(Optional.empty());
            internationalizationService.changeLanguageState(language);
        });
    }

    @Test
    public void shouldThrowAnExceptionWhenDisablingDefaultLanguage(){
        assertThrows(IllegalStateException.class, () -> {
            Internationalization internationalization = new Internationalization(1L, "pl", false, "{\"test\":\"content\"");
            when(repository.findByLanguageOrderByIdDesc(language.getLanguage())).thenReturn(Optional.of(internationalization));
            when(configurationManager.getConfiguration()).thenReturn(new ConfigurationView(false, false, "pl", false));
            internationalizationService.changeLanguageState(language);
        });
    }

    @Test
    public void shouldGetLanguageContent(){
        Internationalization internationalization = new Internationalization(1L, "pl", true, "{\"test\":\"content\"");
        when(repository.findByLanguageOrderByIdDesc("pl")).thenReturn(Optional.of(internationalization));
        assertEquals(internationalization.getContent(), internationalizationService.getLanguageContent("pl"));
    }

    @Test
    public void shouldThrowAnExceptionWhenLanguageIsNotAvailable(){
        assertThrows(IllegalStateException.class, () -> {
            when(repository.findByLanguageOrderByIdDesc(any())).thenReturn(Optional.empty());
            internationalizationService.getLanguageContent("pl");
        });
    }

    @Test
    public void shouldReturnEnabledLanguages(){
        when(repository.findAll()).thenReturn(Collections.singletonList(new Internationalization(1L, "pl", true, "{\"test\":\"content\"")));
        List<String> result = this.internationalizationService.getEnabledLanguages();
        assertEquals(1, result.size());
        assertEquals("pl", result.get(0));
    }

    @Test
    public void shouldReturnEmptyListWhenAllLanguagesDisabled(){
        when(repository.findAll()).thenReturn(Collections.singletonList(new Internationalization(1L, "pl", false, "{\"test\":\"content\"")));
        List<String> result = this.internationalizationService.getEnabledLanguages();
        assertEquals(0, result.size());
    }

    @Test
    void shouldUpdateLanguage(){
        when(repository.findByLanguageOrderByIdDesc(anyString())).thenReturn(Optional.of(new Internationalization(1L, "pl", true, "{\"test\":\"content\"}")));
        this.internationalizationService.updateLanguage("pl", "{\"test\":\"new-content\"}");
        verify(repository, times(1)).save(any());
    }

    @Test
    void shouldNotUpdateLanguageWhenLangIsEmpty(){
        when(repository.findByLanguageOrderByIdDesc(anyString())).thenReturn(Optional.of(new Internationalization(1L, "pl", true, "{\"test\":\"content\"}")));
        assertThrows(IllegalArgumentException.class, () -> this.internationalizationService.updateLanguage("", "{\"test\":\"new-content\"}"));
;
    }

    @Test
    void shouldNotUpdateLanguageWhenContentIsEmpty(){
        when(repository.findByLanguageOrderByIdDesc(anyString())).thenReturn(Optional.of(new Internationalization(1L, "pl", true, "{\"test\":\"content\"}")));
        assertThrows(IllegalArgumentException.class, () -> this.internationalizationService.updateLanguage("pl", ""));
    }

    @Test
    void shouldNotUpdateLanguageWhenContentIsNotValidJson(){
        when(repository.findByLanguageOrderByIdDesc(anyString())).thenReturn(Optional.of(new Internationalization(1L, "pl", true, "{\"test\":\"content\"}")));
        assertThrows(IllegalArgumentException.class, () -> this.internationalizationService.updateLanguage("pl", "{\"test\":\"new-content\""));
    }

    @Test
    void shouldNotUpdateLanguageWhenLangIsNotFound(){
        when(repository.findByLanguageOrderByIdDesc(anyString())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> this.internationalizationService.updateLanguage("pl", "{\"test\":\"new-content\"}"));
    }

    @Test
    void shouldGetLanguage(){
        when(repository.findByLanguageOrderByIdDesc(anyString())).thenReturn(Optional.of(new Internationalization(1L, "pl", true, "{\"test\":\"content\"}")));
        InternationalizationView langView = internationalizationService.getLanguage("pl");
        assertEquals("pl", langView.getLanguage());
        assertTrue(StringUtils.isNotEmpty(langView.getContent()));
    }

    @Test
    void shouldNotGetLanguageWhenLangIsNotExists(){
        when(repository.findByLanguageOrderByIdDesc(anyString())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> this.internationalizationService.getLanguage("pl"));
    }
}
