package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.persistent.entity.Content;
import net.geant.nmaas.portal.persistent.entity.Internationalization;
import net.geant.nmaas.portal.persistent.repositories.ContentRepository;
import net.geant.nmaas.portal.persistent.repositories.InternationalizationRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.inject.Inject;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContentServiceImplTest {

    @Mock
    private ContentRepository contentRepository;

    @InjectMocks
    private ContentServiceImpl contentService;

    @Mock
    private InternationalizationRepository internationalizationRepository;

    @Before
    public void setup(){
        contentService = new ContentServiceImpl(contentRepository, internationalizationRepository);
    }

    @Test
    public void shouldReturnEmptyWhenIdIsNullAtFindById(){
        assertEquals(Optional.empty(), contentService.findById(null));
    }

    @Test
    public void shouldReturnContentWhenIdIsNotNullAtFindById(){
        Content testContent = new Content("test", "test", "test");
        when(contentRepository.findById(anyLong())).thenReturn(Optional.of(testContent));
        assertTrue(contentService.findById((long) 0).isPresent());
    }

    @Test
    public void shouldReturnEmptyWhenNameIsNullAtFindByName(){
        assertEquals(Optional.empty(), contentService.findByName(null));
    }

    @Test
    public void shouldReturnContentWhenIdIsNotNullAtFindByName(){
        Content testContent = new Content("test", "test", "test");
        when(contentRepository.findByName(anyString())).thenReturn(Optional.of(testContent));
        assertTrue(contentService.findByName("test").isPresent());
    }

    @Test
    public void shouldCreateNewContentRecord(){
        when(contentRepository.findByName(anyString())).thenReturn(Optional.empty());
        Content testContent = new Content((long) 0,"testName", "Lorem ipsum", "testTitle");
        when(contentRepository.save(isA(Content.class))).thenReturn(testContent);
        Content result = contentService.createNewContentRecord("testName", "Lorem ipsum", "testTitle");
        assertEquals(0, result.getId().longValue());
    }

    @Test(expected = ObjectAlreadyExistsException.class)
    public void shouldNotCreateNewContentRecordDueToObjectAlreadyExist(){
        when(contentRepository.findByName(anyString())).thenThrow(ObjectAlreadyExistsException.class);
        contentService.createNewContentRecord("test", "lorem ipsum", "test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotCreateNewContentRecordDueToInvalidName(){
        contentService.createNewContentRecord(null, "Lorem ipsum", "testTitle");
    }

    @Test
    public void shouldUpdateContentRecord(){
        Content testContent = new Content((long)0, "testName", "Lorem", "testTitle");
        when(contentRepository.findByName(anyString())).thenReturn(Optional.ofNullable(testContent));
        Optional<Content> testUpdateContent = contentService.findByName("test");
        if(testUpdateContent.isPresent()){
            testUpdateContent.get().setContent("Lorem ipsum dolor sit");
            when(contentRepository.existsById(anyLong())).thenReturn(true);
            when(contentRepository.saveAndFlush(testUpdateContent.get())).thenReturn(testUpdateContent.get());
            contentService.update(testUpdateContent.get());
        }
        assertEquals("Lorem ipsum dolor sit", contentService.findByName("test").get().getContent());
    }

    @Test(expected = net.geant.nmaas.portal.api.exception.ProcessingException.class)
    public void shouldNotAllowToUpdateDueToContentDoNotExist(){
        Content testContent = new Content((long)0, "testName", "Lorem", "testTitle");
        contentService.update(testContent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowToUpdateDueToInvalidId(){
        Content testContent = new Content(null, "testName", "Lorem ipsum", "testTitle");
        contentService.update(testContent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowToUpdateDueToEmptyContent(){
        contentService.update(null);
    }

    @Test
    public void shouldRemoveContentRecord(){
        Content testContent = new Content((long)0, "testName", "Lorem", "testTitle");
        when(contentRepository.existsById(anyLong())).thenReturn(true);
        contentService.delete(testContent);
        verify(contentRepository).delete(testContent);
    }

    @Test(expected = ProcessingException.class)
    public void shouldNotRemoveContentRecordDueToContentDoNotExist(){
        Content testContent = new Content((long)0, "testName", "Lorem", "testTitle");
        contentService.delete(testContent);
        verify(contentRepository).delete(testContent);
    }

    @Test
	public void shouldReturnValueForValidLanguageAndRootAndKey(){
		Internationalization internationalization = Internationalization
				.builder()
				.id(1L)
				.language("en")
				.enabled(true)
				.content("{\"ROOT\":{\"KEY1\":\"VAL1\",\"KEY2\":\"VAL2\"}}")
				.build();
		when(internationalizationRepository.findByLanguageOrderByIdDesc(anyString())).thenReturn(Optional.of(internationalization));

		assertEquals("VAL1", contentService.getContent("en", "ROOT", "KEY1"));
	}

	@Test
	public void shouldReturnERRORForValidLanguageAndRootAndInvalidKey(){
		Internationalization internationalization = Internationalization
				.builder()
				.id(1L)
				.language("en")
				.enabled(true)
				.content("{\"ROOT\":{\"KEY1\":\"VAL1\",\"KEY2\":\"VAL2\"}}")
				.build();
		when(internationalizationRepository.findByLanguageOrderByIdDesc(anyString())).thenReturn(Optional.of(internationalization));

		assertEquals("Enexpected error", contentService.getContent("en", "ROOT", "KEY3"));
	}

	@Test
	public void shouldReturnERRORForInvalidLanguageAndValidRootAndKey(){
		when(internationalizationRepository.findByLanguageOrderByIdDesc(anyString())).thenReturn(Optional.empty());
		assertEquals("Enexpected error - invalid language", contentService.getContent("InvalidLanguage", "ROOT", "KEY3"));
	}

	@Test
	public void shouldReturnERRORForValidLanguageAndInvalidRootAndValidKey(){
		Internationalization internationalization = Internationalization
				.builder()
				.id(1L)
				.language("en")
				.enabled(true)
				.content("{\"ROOT\":{\"KEY1\":\"VAL1\",\"KEY2\":\"VAL2\"}}")
				.build();
		when(internationalizationRepository.findByLanguageOrderByIdDesc(anyString())).thenReturn(Optional.of(internationalization));

		assertEquals("Enexpected error", contentService.getContent("en", "INVALIDROOT", "KEY2"));
	}

}
