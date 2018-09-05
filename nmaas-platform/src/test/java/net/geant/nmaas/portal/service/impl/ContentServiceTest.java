package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Content;
import net.geant.nmaas.portal.service.ContentService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ContentServiceTest {
    @Autowired
    private ContentService contentService;

    @Autowired
    private ContentServiceImpl contentServiceImpl;

    @Test(expected = IllegalArgumentException.class)
    public void testCheckParamIdShouldThrowException(){
        contentServiceImpl.checkParam((Long) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckParamStringShouldThrowException(){
        contentServiceImpl.checkParam((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckParamContentShouldThrowException(){
        Content testContent = null;
        contentServiceImpl.checkParam(testContent);
    }

    @Test
    public void shouldCreateNewContentRecord(){
        contentService.createNewContentRecord("test", "Lorem ipsum", "test");
        assertEquals("test", contentService.findByName("test").get().getTitle());
        assertEquals("Lorem ipsum", contentService.findByName("test").get().getContent());
        assertEquals("test", contentService.findById((long) 0).get().getName());
    }

    @Test(expected = ObjectAlreadyExistsException.class)
    public void shouldNotCreateNewContentRecordDueToObjectAlreadyExist(){
        contentService.createNewContentRecord("test", "Lorem ipsum", "test");
        contentService.createNewContentRecord("test", "Lorem ipsum2", "test2");
    }

    @Test
    public void shouldUpdateContentRecord(){
        contentService.createNewContentRecord("test", "Lorem ipsum", "test");
        Optional<Content> testUpdateContent = contentService.findByName("test");
        if(testUpdateContent.isPresent()){
            testUpdateContent.get().setContent("Lorem ipsum dolor sit");
            contentService.update(testUpdateContent.get());
        }
        assertEquals("Lorem ipsum dolor sit", contentService.findByName("test").get().getContent());
    }

    @Test(expected = ProcessingException.class)
    public void shouldNotAllowToUpdateDueToContentDoNotExist(){
        Content testContent = new Content("testName", "Lorem", "testTitle");
        contentService.update(testContent);
    }



}
