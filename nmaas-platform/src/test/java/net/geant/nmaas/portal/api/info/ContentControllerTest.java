package net.geant.nmaas.portal.api.info;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.geant.nmaas.portal.api.domain.ContentView;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Content;
import net.geant.nmaas.portal.persistent.entity.Internationalization;
import net.geant.nmaas.portal.persistent.repositories.ContentRepository;
import net.geant.nmaas.portal.persistent.repositories.InternationalizationRepository;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.modelmapper.ModelMapper;

public class ContentControllerTest {

    private ContentRepository repository = mock(ContentRepository.class);

    private InternationalizationRepository internationalizationRepository = mock(InternationalizationRepository.class);

    private ModelMapper modelMapper = new ModelMapper();

    private ContentController contentController;

    private Content content;

    private Internationalization internationalization;

    @Before
    public void setup(){
        this.contentController = new ContentController(repository, internationalizationRepository, modelMapper);
        this.content = new Content(1L, "Test name", "Test content", "Test title");
        this.internationalization = new Internationalization(1L, "pl", true, "Test content");
    }

    @Test
    public void shouldGetContent(){
        when(repository.findByName(content.getName())).thenReturn(Optional.of(content));
        ContentView contentView = this.contentController.getContent(content.getName());
        assertEquals(content.getName(), contentView.getName());
        assertEquals(content.getContent(), contentView.getContent());
        assertEquals(content.getTitle(), contentView.getTitle());
    }

    @Test(expected = ProcessingException.class)
    public void shouldThrowAnExceptionWhenContentNotFound(){
        when(repository.findByName(content.getName())).thenReturn(Optional.empty());
        contentController.getContent(content.getName());
    }

    @Test
    public void shouldGetLanguage(){
        when(internationalizationRepository.findByLanguageOrderByIdDesc("pl")).thenReturn(Optional.of(internationalization));
        assertEquals(internationalization.getContent(), contentController.getLanguage("pl"));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowAnExceptionWhenLanguageIsNotAvaliable(){
        when(internationalizationRepository.findByLanguageOrderByIdDesc(any())).thenReturn(Optional.empty());
        contentController.getLanguage("pl");
    }

    @Test
    public void shouldReturnEnabledLanguages(){
        when(internationalizationRepository.findAll()).thenReturn(Collections.singletonList(internationalization));
        List<String> result = this.contentController.getEnabledLanguages();
        assertEquals(1, result.size());
        assertEquals("pl", result.get(0));
    }

    @Test
    public void shouldReturnEmptyListWhenAllLanguagesDisabled(){
        internationalization.setEnabled(false);
        List<String> result = this.contentController.getEnabledLanguages();
        assertEquals(0, result.size());
    }
}
