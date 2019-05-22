package net.geant.nmaas.portal.api.info;

import net.geant.nmaas.portal.api.domain.ContentView;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Content;
import net.geant.nmaas.portal.persistent.repositories.ContentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContentControllerTest {

    private ContentRepository repository = mock(ContentRepository.class);

    private ModelMapper modelMapper = new ModelMapper();

    private ContentController contentController;

    private Content content;

    @BeforeEach
    public void setup(){
        this.contentController = new ContentController(repository, modelMapper);
        this.content = new Content(1L, "Test name", "Test content", "Test title");
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

}
