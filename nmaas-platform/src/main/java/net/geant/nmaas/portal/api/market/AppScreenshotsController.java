package net.geant.nmaas.portal.api.market;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.geant.nmaas.portal.api.domain.UserFile;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.StorageException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.FileInfo;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import net.geant.nmaas.portal.service.FileStorageService;


@RestController
@RequestMapping("/portal/api/apps/{appId}")
public class AppScreenshotsController extends AppBaseController {
	
	@Autowired
	private FileStorageService fileStorage;	
	
	@RequestMapping(value="/logo", method=RequestMethod.GET)
	public ResponseEntity<InputStreamResource> getLogo(@PathVariable("appId") Long appId) throws MissingElementException, FileNotFoundException {
		Application app = getApp(appId);
		
		if(app.getLogo() != null)
			return getFile(app.getLogo());
		
		throw new MissingElementException("No logo found");
	}
	
	@RequestMapping(value="/logo", method=RequestMethod.POST)
	@PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public net.geant.nmaas.portal.api.domain.FileInfo uploadLogo(@PathVariable("appId") Long appId, @RequestParam("file") MultipartFile file) throws MissingElementException, FileNotFoundException, StorageException {
		Application app = getApp(appId);
		
		if(app.getLogo() != null) {
			fileStorage.remove(app.getLogo());
			app.setLogo(null);
			applications.update(app);
		}
		
		FileInfo fileInfo = fileStorage.store(file);
		app.setLogo(fileInfo);
		applications.update(app);
		
		return modelMapper.map(fileInfo, net.geant.nmaas.portal.api.domain.FileInfo.class);
	}
	
	@RequestMapping(value="/logo", method=RequestMethod.DELETE)
	@PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public void deleteLogo(@PathVariable("appId") Long appId) throws MissingElementException, FileNotFoundException, StorageException {
		Application app = getApp(appId);
		
		if(app.getLogo() != null) {
			fileStorage.remove(app.getLogo());
			app.setLogo(null);
			applications.update(app);
		}
	}
	
	@RequestMapping(value="/screenshots", method=RequestMethod.GET)
	public List<UserFile> getScreenshotsInfo(@PathVariable("appId") Long appId) throws MissingElementException {
		Application app = getApp(appId);

		return app.getScreenshots()
					.stream()
					.map(screenshot -> modelMapper.map(screenshot, UserFile.class))
					.collect(Collectors.toList());
	}	
	
	@RequestMapping(value="/screenshots", method=RequestMethod.POST)
	@PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public net.geant.nmaas.portal.api.domain.FileInfo uploadScreenshot(@PathVariable("appId") Long appId, @RequestParam("file") MultipartFile file) throws StorageException, MissingElementException {
		Application app = getApp(appId);
		
		FileInfo fileInfo = fileStorage.store(file);
		app.getScreenshots().add(fileInfo);
		applications.update(app);
		
		return modelMapper.map(fileInfo, net.geant.nmaas.portal.api.domain.FileInfo.class);
	}

	@RequestMapping(value="/screenshots/{screenshotId}", method=RequestMethod.GET)
	public ResponseEntity<InputStreamResource> getScreenshot(@PathVariable("appId") Long appId, @PathVariable("screenshotId") Long screenshotId) throws MissingElementException, FileNotFoundException {
		Application app = getApp(appId);
		
		for(net.geant.nmaas.portal.persistent.entity.FileInfo screenshot : app.getScreenshots()) {
			if(screenshot.getId() == screenshotId) {
				FileInfo imageFile = screenshot;
				
				return getFile(imageFile);
			}
		}
		throw new MissingElementException("Screenshot id= " + screenshotId + " for app id=" + appId + " not found.");
	}

	@RequestMapping(value="/screenshots/{screenshotId}", method=RequestMethod.DELETE)
	@PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public void deleteScreenshot(@PathVariable(value="appId", required=true) Long appId, @PathVariable(value="screenshotId", required=true) Long screenshotId) throws MissingElementException, StorageException {
		Application app = getApp(appId);
		FileInfo screenshotInfo = getScreenshot(app, screenshotId);
		
		fileStorage.remove(screenshotInfo);
		app.getScreenshots().remove(screenshotInfo);
	}	
	
	private ResponseEntity<InputStreamResource> getFile(FileInfo imageFile)
			throws MissingElementException, FileNotFoundException {
		File file = fileStorage.getFile(imageFile.getId());
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType(imageFile.getContentType()));
		headers.setContentLength(file.length());
		
		InputStreamResource streamFile = new InputStreamResource(new FileInputStream(file));
		return new ResponseEntity<InputStreamResource>(streamFile, headers, HttpStatus.OK);
	}
	

	private FileInfo getScreenshot(Application app, Long screenshotId) throws MissingElementException {
		for(FileInfo screenshot : app.getScreenshots()) {
			if(screenshot.getId() == screenshotId)
				return screenshot;
		}
		throw new MissingElementException("Screenshot id= " + screenshotId + " for app id=" + app.getId() + " not found.");
	}	
}