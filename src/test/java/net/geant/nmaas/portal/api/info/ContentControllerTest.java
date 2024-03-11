package net.geant.nmaas.portal.api.info;

import net.geant.nmaas.portal.api.domain.ContentView;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Content;
import net.geant.nmaas.portal.persistent.repositories.ContentRepository;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContentControllerTest {

    private static final Content CONTENT = new Content(1L, "Test name", "Test content", "Test title");

    private final ContentRepository repository = mock(ContentRepository.class);
    private final ModelMapper modelMapper = new ModelMapper();

    private final ContentController contentController = new ContentController(repository, modelMapper);

    @Test
    public void shouldGetContent(){
        when(repository.findByName(CONTENT.getName())).thenReturn(Optional.of(CONTENT));
        ContentView contentView = this.contentController.getContent(CONTENT.getName());
        assertEquals(CONTENT.getName(), contentView.getName());
        assertEquals(CONTENT.getContent(), contentView.getContent());
        assertEquals(CONTENT.getTitle(), contentView.getTitle());
    }

    @Test
    public void shouldThrowAnExceptionWhenContentNotFound(){
        assertThrows(ProcessingException.class, () -> {
            when(repository.findByName(CONTENT.getName())).thenReturn(Optional.empty());
            contentController.getContent(CONTENT.getName());
        });
    }

}
