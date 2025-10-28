package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.persistence.entity.Content;
import net.geant.nmaas.portal.persistence.repositories.ContentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentServiceImplTest {

    @Mock
    private ContentRepository contentRepository;

    @InjectMocks
    private ContentServiceImpl contentService;

    @BeforeEach
    void setup() {
        contentService = new ContentServiceImpl(contentRepository);
    }

    @Test
    void shouldReturnEmptyWhenIdIsNullAtFindById() {
        assertEquals(Optional.empty(), contentService.findById(null));
    }

    @Test
    void shouldReturnContentWhenIdIsNotNullAtFindById() {
        Content testContent = new Content("test", "test", "test");
        when(contentRepository.findById(anyLong())).thenReturn(Optional.of(testContent));
        assertTrue(contentService.findById((long) 0).isPresent());
    }

    @Test
    void shouldReturnEmptyWhenNameIsNullAtFindByName() {
        assertEquals(Optional.empty(), contentService.findByName(null));
    }

    @Test
    void shouldReturnContentWhenIdIsNotNullAtFindByName() {
        Content testContent = new Content("test", "test", "test");
        when(contentRepository.findByName(anyString())).thenReturn(Optional.of(testContent));
        assertTrue(contentService.findByName("test").isPresent());
    }

    @Test
    void shouldCreateNewContentRecord() {
        when(contentRepository.findByName(anyString())).thenReturn(Optional.empty());
        Content testContent = new Content((long) 0, "testName", "Lorem ipsum", "testTitle");
        when(contentRepository.save(isA(Content.class))).thenReturn(testContent);
        Content result = contentService.createNewContentRecord("testName", "Lorem ipsum", "testTitle");
        assertEquals(0, result.getId().longValue());
    }

    @Test
    void shouldNotCreateNewContentRecordDueToObjectAlreadyExist() {
        when(contentRepository.findByName(anyString())).thenThrow(ObjectAlreadyExistsException.class);
        assertThrows(ObjectAlreadyExistsException.class, () ->
            contentService.createNewContentRecord("test", "lorem ipsum", "test")
        );
    }

    @Test
    void shouldNotCreateNewContentRecordDueToInvalidName() {
        assertThrows(IllegalArgumentException.class, () ->
            contentService.createNewContentRecord(null, "Lorem ipsum", "testTitle")
        );
    }

    @Test
    void shouldUpdateContentRecord() {
        Content testContent = new Content((long) 0, "testName", "Lorem", "testTitle");
        when(contentRepository.findByName(anyString())).thenReturn(Optional.of(testContent));
        Optional<Content> testUpdateContent = contentService.findByName("test");
        if (testUpdateContent.isPresent()) {
            testUpdateContent.get().setContent("Lorem ipsum dolor sit");
            when(contentRepository.existsById(anyLong())).thenReturn(true);
            when(contentRepository.saveAndFlush(testUpdateContent.get())).thenReturn(testUpdateContent.get());
            contentService.update(testUpdateContent.get());
        }
        assertEquals("Lorem ipsum dolor sit", contentService.findByName("test").get().getContent());
    }

    @Test
    void shouldNotAllowToUpdateDueToContentDoNotExist() {
        Content testContent = new Content((long) 0, "testName", "Lorem", "testTitle");
        assertThrows(ProcessingException.class, () ->
                contentService.update(testContent)
        );
    }

    @Test
    void shouldNotAllowToUpdateDueToInvalidId() {
        Content testContent = new Content(null, "testName", "Lorem ipsum", "testTitle");
        assertThrows(IllegalArgumentException.class, () ->
                contentService.update(testContent)
        );
    }

    @Test
    void shouldNotAllowToUpdateDueToEmptyContent() {
        assertThrows(IllegalArgumentException.class, () ->
                contentService.update(null)
        );
    }

    @Test
    void shouldRemoveContentRecord() {
        Content testContent = new Content((long) 0, "testName", "Lorem", "testTitle");
        when(contentRepository.existsById(anyLong())).thenReturn(true);
        contentService.delete(testContent);
        verify(contentRepository).delete(testContent);
    }

    @Test
    void shouldNotRemoveContentRecordDueToContentDoNotExist() {
        Content testContent = new Content((long) 0, "testName", "Lorem", "testTitle");
        assertThrows(ProcessingException.class, () -> {
            contentService.delete(testContent);
            verify(contentRepository).delete(testContent);
        });
    }

}
