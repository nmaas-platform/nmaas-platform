package net.geant.nmaas.portal.api.i18n;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.geant.nmaas.portal.api.i18n.api.LanguageView;
import net.geant.nmaas.portal.persistent.entity.Internationalization;
import net.geant.nmaas.portal.persistent.repositories.InternationalizationRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.modelmapper.ModelMapper;

public class InternationalizationControllerTest {

    private InternationalizationRepository repository = mock(InternationalizationRepository.class);

    private ConfigurationManager configurationManager = mock(ConfigurationManager.class);

    private ModelMapper modelMapper = new ModelMapper();

    private InternationalizationController internationalizationController;

    private LanguageView language;

    @Before
    public void setup(){
        this.internationalizationController = new InternationalizationController(repository, configurationManager, modelMapper);
        this.language = new LanguageView("pl", true);
    }

    @Test
    public void shouldSaveLanguageContent(){
        internationalizationController.saveLanguageContent("pl", "Test content");
        verify(internationalizationController, times(1)).saveLanguageContent("pl", "Test content");
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
        when(configurationManager.getConfiguration().getDefaultLanguage()).thenReturn("fr");
        Internationalization internationalization = new Internationalization(1L, "pl", false, "Test content");
        when(repository.findByLanguageOrderByIdDesc(language.getLanguage())).thenReturn(Optional.of(internationalization));
        internationalizationController.changeSupportedLanguageState(language);
        verify(repository, times(1)).save(any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowAnExceptionWhenLanguageIsNotFound(){
        when(repository.findByLanguageOrderByIdDesc(language.getLanguage())).thenReturn(Optional.empty());
        internationalizationController.changeSupportedLanguageState(language);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowAnExceptionWhenDisablingDefaultLanguage(){
        Internationalization internationalization = new Internationalization(1L, "pl", false, "Test content");
        when(repository.findByLanguageOrderByIdDesc(language.getLanguage())).thenReturn(Optional.of(internationalization));
        when(configurationManager.getConfiguration().getDefaultLanguage()).thenReturn("pl");
        internationalizationController.changeSupportedLanguageState(language);
    }
}
