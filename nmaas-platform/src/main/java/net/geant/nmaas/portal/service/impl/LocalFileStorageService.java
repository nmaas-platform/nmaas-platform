package net.geant.nmaas.portal.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.StorageException;
import net.geant.nmaas.portal.persistent.entity.FileInfo;
import net.geant.nmaas.portal.persistent.repositories.FileInfoRepository;
import net.geant.nmaas.portal.service.FileStorageService;

@Service
public class LocalFileStorageService implements FileStorageService {

	@Value("${upload.dir}")
	String uploadDir;
	
	@Autowired
	FileInfoRepository fileRepo;
	
	@Override
	public FileInfo store(MultipartFile file) throws StorageException {
		Path path = null;
		try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + file.getOriginalFilename());
            }
            
            FileInfo fileInfo = new FileInfo(file.getOriginalFilename(), file.getContentType());
            fileInfo = fileRepo.save(fileInfo);

            path = Paths.get(uploadDir + File.separator + fileInfo.getId());
            Files.copy(file.getInputStream(), getPath(fileInfo.getId()));
            
            return fileInfo;
        } catch (IOException e) {
        	if(path != null)
				try {
					Files.deleteIfExists(path);
				} catch (IOException e1) {
					
				}
            throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
        }
	}

	@Override
	public FileInfo getFileInfo(Long id) {
		return fileRepo.getOne(id);
	}

	@Override
	public File getFile(Long id) throws MissingElementException {
		if(this.getFileInfo(id) != null)
			return new File(uploadDir + File.separator + id);
		throw new MissingElementException("File id=" + id + " not found.");
	}

	@Override
	public boolean remove(FileInfo fileInfo) throws MissingElementException, StorageException {
		if(fileInfo != null && fileRepo.existsById(fileInfo.getId())) {
			fileRepo.delete(fileInfo);
		
			try {
				Files.deleteIfExists(getPath(fileInfo.getId()));
			} catch (IOException e) {
				throw new StorageException("Unable to delete file id=" + fileInfo.getId());
			}
			
			return true;
		}
		return false;
	}

	private Path getPath(Long id) {
		return (id != null ? Paths.get(uploadDir + File.separator + id) : null);
	}
	
}
