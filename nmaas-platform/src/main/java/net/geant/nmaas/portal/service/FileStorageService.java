package net.geant.nmaas.portal.service;

import java.io.File;

import org.springframework.web.multipart.MultipartFile;

import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.StorageException;
import net.geant.nmaas.portal.persistent.entity.FileInfo;

public interface FileStorageService {

	FileInfo store(MultipartFile file) throws StorageException;
	File getFile(Long id) throws MissingElementException;
	FileInfo getFileInfo(Long id);
	boolean remove(FileInfo fileInfo) throws MissingElementException, StorageException;
}
