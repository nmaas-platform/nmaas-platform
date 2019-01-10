package net.geant.nmaas.portal.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.transaction.Transactional;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import net.geant.nmaas.SecurityConfig;
import net.geant.nmaas.portal.PersistentConfig;
import net.geant.nmaas.portal.PortalConfig;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.StorageException;
import net.geant.nmaas.portal.persistent.entity.FileInfo;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {SecurityConfig.class, PortalConfig.class, PersistentConfig.class})
@EnableAutoConfiguration
@Transactional
@Rollback
public class LocalFileStorageServiceTest {

	@Autowired
	private LocalFileStorageService storage;
	
	@Value("${upload.dir}")
	String uploadDir;
	
	final String content = "FAKE SCREENSHOT";
	
	Path path;
	
	@Before
	public void setUp() {
		path = null;
	}
	
	@After
	public void tearDown() throws IOException {
		if (path != null ) 
			Files.deleteIfExists(path);
	}
	
	/*
	 * TODO: update with powermock
	 */
	@Test
	@Ignore
	public void testStoreGetRemove() throws StorageException, MissingElementException, FileNotFoundException, IOException {
        MockMultipartFile multipartFile =
                new MockMultipartFile("file", "test.txt", "text/plain", "FAKE SCREENSHOT".getBytes());
        
        FileInfo fileInfo = storage.store(multipartFile);
        
        assertNotNull(fileInfo);
        
        path = Paths.get(uploadDir + File.separatorChar + fileInfo.getId());
        assertTrue(Files.exists(path));
       
        byte[] readContent = Files.readAllBytes(path);
        new String(readContent);
        assertEquals(content, new String(readContent));

        boolean deleted = storage.remove(fileInfo);

        assertTrue(deleted);
        assertFalse(Files.exists(path));        
	}
	
	
}
