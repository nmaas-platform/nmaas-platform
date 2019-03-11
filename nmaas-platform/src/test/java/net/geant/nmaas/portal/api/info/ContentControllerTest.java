package net.geant.nmaas.portal.api.info;

import net.geant.nmaas.portal.api.domain.ContentView;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Content;
import net.geant.nmaas.portal.persistent.entity.Internationalization;
import net.geant.nmaas.portal.persistent.repositories.ContentRepository;
import net.geant.nmaas.portal.persistent.repositories.InternationalizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContentControllerTest {

    private ContentRepository repository = mock(ContentRepository.class);

    private InternationalizationRepository internationalizationRepository = mock(InternationalizationRepository.class);

    private ModelMapper modelMapper = new ModelMapper();

    private ContentController contentController;

    private Content content;

    private Internationalization internationalization;

    @BeforeEach
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

    @Test
    public void shouldThrowAnExceptionWhenContentNotFound(){
        assertThrows(ProcessingException.class, () -> {
            when(repository.findByName(content.getName())).thenReturn(Optional.empty());
            contentController.getContent(content.getName());
        });
    }

    @Test
    public void shouldGetLanguage(){
        when(internationalizationRepository.findByLanguageOrderByIdDesc("pl")).thenReturn(Optional.of(internationalization));
        assertEquals(internationalization.getContent(), contentController.getLanguage("pl"));
    }

    @Test
    public void shouldThrowAnExceptionWhenLanguageIsNotAvailable(){
        assertThrows(IllegalStateException.class, () -> {
            when(internationalizationRepository.findByLanguageOrderByIdDesc(any())).thenReturn(Optional.empty());
            contentController.getLanguage("pl");
        });
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
