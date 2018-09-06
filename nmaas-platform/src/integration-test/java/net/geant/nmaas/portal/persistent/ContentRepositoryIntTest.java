package net.geant.nmaas.portal.persistent;


import net.geant.nmaas.portal.PersistentConfig;
import net.geant.nmaas.portal.persistent.entity.Content;
import net.geant.nmaas.portal.persistent.repositories.ContentRepository;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes={PersistentConfig.class}/* loader = AnnotationConfigContextLoader.class, */ )
@EnableAutoConfiguration
@Transactional
@Rollback
public class ContentRepositoryIntTest {

    @Autowired
    ContentRepository contentRepository;

    @Before
    public void setUp() throws Exception {
        contentRepository.deleteAll();
    }

    @Test
    public void createContentTest(){
        Content testContent = new Content("test1", "createTestContent", "Test content with id=1");
        contentRepository.save(testContent);

        assertEquals(1, contentRepository.count());

        Optional<Content> firstTestContent = contentRepository.findByName("test1");
        assertNotNull(firstTestContent);
        assertEquals("test1", firstTestContent.get().getName());
        assertEquals("createTestContent", firstTestContent.get().getTitle());
        assertEquals("Test content with id=1", firstTestContent.get().getContent());

        contentRepository.delete(firstTestContent.get());

        assertEquals(0, contentRepository.count());
    }

}
