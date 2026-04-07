package net.geant.nmaas.portal.api.apps;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.api.dto.FileInfoDto;
import net.geant.nmaas.api.dto.users.UserFileDto;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.FileInfo;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.FileStorageService;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/api/apps/{appId}")
@Slf4j
public class AppScreenshotsController extends AppBaseController {

    private final FileStorageService fileStorage;

    @Autowired
    public AppScreenshotsController(ModelMapper modelMapper, ApplicationService applicationService, ApplicationBaseService appBaseService, UserService userService, FileStorageService fileStorage) {
        super(modelMapper, userService, applicationService, appBaseService);
        this.fileStorage = fileStorage;
    }

    @GetMapping("/logo")
    public ResponseEntity<InputStreamResource> getLogo(@PathVariable("appId") Long appId) throws FileNotFoundException {
        ApplicationBase app = getBaseApp(appId);
        if (app.getLogo() != null) {
            return getFile(app.getLogo());
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/logo")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @Transactional
    public FileInfoDto uploadLogo(@PathVariable("appId") Long appId, @RequestParam("file") MultipartFile file) {
        ApplicationBase app = getBaseApp(appId);

        if (app.getLogo() != null) {
            fileStorage.remove(app.getLogo());
            app.setLogo(null);
            appBaseService.update(app);
        }

        FileInfo fileInfo = fileStorage.store(file);
        app.setLogo(fileInfo);
        appBaseService.update(app);

        return modelMapper.map(fileInfo, FileInfoDto.class);
    }

    @DeleteMapping("/logo")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @Transactional
    public void deleteLogo(@PathVariable("appId") Long appId) {
        ApplicationBase app = getBaseApp(appId);
        if (app.getLogo() != null) {
            fileStorage.remove(app.getLogo());
            app.setLogo(null);
            appBaseService.update(app);
        }
    }

    @GetMapping("/screenshots")
    public List<UserFileDto> getScreenshotsInfo(@PathVariable("appId") Long appId) {
        ApplicationBase app = getBaseApp(appId);
        return app.getScreenshots().stream()
                .map(screenshot -> modelMapper.map(screenshot, UserFileDto.class))
                .toList();
    }

    @PostMapping("/screenshots")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @Transactional
    public FileInfoDto uploadScreenshot(@PathVariable("appId") Long appId, @RequestParam("file") MultipartFile file) {
        ApplicationBase app = getBaseApp(appId);

        FileInfo fileInfo = fileStorage.store(file);
        app.getScreenshots().add(fileInfo);
        appBaseService.update(app);

        return modelMapper.map(fileInfo, FileInfoDto.class);
    }

    @GetMapping("/screenshots/{screenshotId}")
    public ResponseEntity<InputStreamResource> getScreenshot(@PathVariable("appId") Long appId, @PathVariable("screenshotId") Long screenshotId) throws FileNotFoundException {
        ApplicationBase app = getBaseApp(appId);
        for (FileInfo screenshot : app.getScreenshots()) {
            if (screenshot.getId().equals(screenshotId)) {
                return getFile(screenshot);
            }
        }
        throw new MissingElementException("Screenshot id= " + screenshotId + " for app id=" + appId + " not found.");
    }

    @DeleteMapping("/screenshots/{screenshotId}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @Transactional
    public void deleteScreenshot(@PathVariable(value = "appId") Long appId, @PathVariable(value = "screenshotId") Long screenshotId) {
        ApplicationBase app = getBaseApp(appId);
        FileInfo screenshotInfo = getScreenshot(app, screenshotId);

        fileStorage.remove(screenshotInfo);
        app.getScreenshots().remove(screenshotInfo);
        appBaseService.update(app);
    }

    @DeleteMapping("/screenshots/all")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @Transactional
    public void deleteScreenshots(@PathVariable(value = "appId") Long appId) {
        ApplicationBase app = getBaseApp(appId);
        app.getScreenshots().forEach(fileStorage::remove);
        app.getScreenshots().clear();
        appBaseService.update(app);
    }

    private ResponseEntity<InputStreamResource> getFile(FileInfo imageFile) throws FileNotFoundException {
        File file = fileStorage.getFile(imageFile.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(imageFile.getContentType()));
        headers.setContentLength(file.length());

        InputStreamResource streamFile = new InputStreamResource(new FileInputStream(file));
        return new ResponseEntity<>(streamFile, headers, HttpStatus.OK);
    }

    private FileInfo getScreenshot(ApplicationBase app, Long screenshotId) {
        for (FileInfo screenshot : app.getScreenshots()) {
            if (screenshot.getId().equals(screenshotId)) {
                return screenshot;
            }
        }
        throw new MissingElementException("Screenshot id= " + screenshotId + " for app id=" + app.getId() + " not found");
    }
}
