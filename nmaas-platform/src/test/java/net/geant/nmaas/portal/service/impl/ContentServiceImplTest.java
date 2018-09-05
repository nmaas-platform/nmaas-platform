package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.persistent.entity.Content;
import net.geant.nmaas.portal.persistent.repositories.ContentRepository;
import net.geant.nmaas.portal.service.ContentService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContentServiceImplTest {

    @Mock
    private ContentRepository contentRepository;

    @InjectMocks
    private ContentServiceImpl contentService;

    @Before
    public void setup(){
        contentService = new ContentServiceImpl(contentRepository);
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

    @Test
    public void shouldUpdateContentRecord(){
        Content testContent = new Content((long)0, "testName", "Lorem", "testTitle");
        when(contentRepository.findByName(anyString())).thenReturn(Optional.ofNullable(testContent));
        Optional<Content> testUpdateContent = contentService.findByName("test");
        if(testUpdateContent.isPresent()){
            testUpdateContent.get().setContent("Lorem ipsum dolor sit");
            //        if(!contentRepo.existsById(content.getId())){
            //            throw new ProcessingException("Content (id=" + content.getId() + ") does not exists.");
            //        }
            //
            //        contentRepo.saveAndFlush(content);
            when(contentRepository.existsById(anyLong())).thenReturn(true);
            when(contentRepository.saveAndFlush(testUpdateContent.get())).thenReturn(testUpdateContent.get());
            contentService.update(testUpdateContent.get());
        }
        assertEquals("Lorem ipsum dolor sit", contentService.findByName("test").get().getContent());
    }

    @Test(expected = net.geant.nmaas.portal.api.exception.ProcessingException.class)
    public void shouldNotAllowToUpdateDueToContentDoNotExist(){
        //when(contentRepository.findByName(anyString())).thenThrow(net.geant.nmaas.portal.api.exception.ProcessingException.class);
        Content testContent = new Content((long)0, "testName", "Lorem", "testTitle");
        contentService.update(testContent);
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
        //when(contentRepository.existsByName(anyString())).thenThrow(net.geant.nmaas.portal.api.exception.ProcessingException.class);
        contentService.delete(testContent);
        verify(contentRepository).delete(testContent);
    }

}
