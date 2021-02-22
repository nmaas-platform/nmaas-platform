package net.geant.nmaas.portal.service;

import java.io.File;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

import net.geant.nmaas.portal.persistent.entity.FileInfo;

public interface FileStorageService {

	FileInfo store(MultipartFile file);
	File getFile(Long id);
	FileInfo getFileInfo(Long id);
	List<FileInfo> getFileInfoByContentType(String contentType);
	boolean remove(FileInfo fileInfo);
}
