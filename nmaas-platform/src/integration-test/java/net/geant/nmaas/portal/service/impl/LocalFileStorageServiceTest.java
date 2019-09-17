package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.StorageException;
import net.geant.nmaas.portal.persistent.entity.FileInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
@Rollback
public class LocalFileStorageServiceTest {

	@Autowired
	private LocalFileStorageService storage;
	
	@Value("${upload.dir}")
	private String uploadDir;
	
	private final String content = "FAKE SCREENSHOT";
	
	private Path path;
	
	@BeforeEach
	public void setUp() {
		path = null;
	}
	
	@AfterEach
	public void tearDown() throws IOException {
		if (path != null ) 
			Files.deleteIfExists(path);
	}

	@Disabled
	@Test
	public void testStoreGetRemove() throws StorageException, MissingElementException, IOException {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", content.getBytes());
        
        FileInfo fileInfo = storage.store(multipartFile);
        
        assertNotNull(fileInfo);
        
        path = Paths.get(uploadDir + File.separatorChar + fileInfo.getId());
        assertTrue(Files.exists(path));

        assertEquals(content, Files.readAllLines(path).get(0));

        boolean deleted = storage.remove(fileInfo);

        assertTrue(deleted);
        assertFalse(Files.exists(path));        
	}

}
