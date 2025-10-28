package net.geant.nmaas.portal.api.market;

import net.geant.nmaas.portal.domain.FileInfoView;
import net.geant.nmaas.portal.domain.UserFile;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.FileInfo;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.FileStorageService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppScreenshotsControllerTest {

    private final ApplicationService applicationService = mock(ApplicationService.class);
    private final ApplicationBaseService applicationBaseService = mock(ApplicationBaseService.class);
    private final UserService userService = mock(UserService.class);
    private final FileStorageService fileStorageService = mock(FileStorageService.class);

    private AppScreenshotsController appScreenshotsController;

    private ApplicationBase app;
    private ApplicationBase appWithLogo;

    @BeforeEach
    void setup() {
        this.appScreenshotsController = new AppScreenshotsController(
                new ModelMapper(),
                applicationService,
                applicationBaseService,
                userService,
                fileStorageService);

        app = new ApplicationBase(1L, "name");
        when(applicationBaseService.findByName("name")).thenReturn(app);
        when(applicationBaseService.getBaseApp(1L)).thenReturn(app);

        appWithLogo = new ApplicationBase(2L, "appWithLogo");

        FileInfo logo = new FileInfo("logo", "image/png");
        logo.setId(1L);
        File f = mock(File.class);
        when(f.length()).thenReturn(30L);
        appWithLogo.setLogo(logo);
        when(fileStorageService.getFile(1L)).thenReturn(f);

        List<FileInfo> screenshots = new ArrayList<>();
        screenshots.add(new FileInfo("s1", "image/png"));
        screenshots.get(0).setId(0L);
        screenshots.add(new FileInfo("s2", "image/png"));
        screenshots.get(1).setId(1L);

        appWithLogo.setScreenshots(screenshots);

        when(applicationBaseService.findByName("appWithLogo")).thenReturn(appWithLogo);
        when(applicationBaseService.getBaseApp(2L)).thenReturn(appWithLogo);
    }

    @Test
    void shouldGetErrorWhenAppLogoIsNotDefined() {
        ResponseEntity<InputStreamResource> re = null;
        try {
            re = this.appScreenshotsController.getLogo(app.getId());
        } catch (FileNotFoundException e) {
            fail();
        }
        assertNotNull(re);
        assertEquals(HttpStatus.NOT_FOUND, re.getStatusCode());

    }

    @Test
    void shouldUpdateLogoWhenExists() {

        MultipartFile mf = mock(MultipartFile.class);
        FileInfo newLogo = new FileInfo("newLogo", "image/png");
        when(fileStorageService.store(mf)).thenReturn(newLogo);

        FileInfoView fiv = this.appScreenshotsController.uploadLogo(appWithLogo.getId(), mf);

        assertEquals(newLogo.getFilename(), fiv.getFilename());
        verify(applicationBaseService, times(2)).update(any(ApplicationBase.class));
        verify(fileStorageService, times(1)).remove(any(FileInfo.class));
    }

    @Test
    void shouldCreateLogoWhenNotExists() {
        MultipartFile mf = mock(MultipartFile.class);
        FileInfo newLogo = new FileInfo("newLogo", "image/png");
        when(fileStorageService.store(mf)).thenReturn(newLogo);

        FileInfoView fiv = this.appScreenshotsController.uploadLogo(app.getId(), mf);

        assertEquals(newLogo.getFilename(), fiv.getFilename());
        verify(applicationBaseService, times(1)).update(any(ApplicationBase.class));
    }

    @Test
    void shouldDeleteLogoIfExists() {

        this.appScreenshotsController.deleteLogo(appWithLogo.getId());

        verify(fileStorageService, times(1)).remove(any(FileInfo.class));
        verify(applicationBaseService, times(1)).update(any(ApplicationBase.class));
    }

    @Test
    void onDeleteShouldDoNothingWhenLogoDoesNotExists() {
        this.appScreenshotsController.deleteLogo(app.getId());

        verify(fileStorageService, times(0)).remove(any(FileInfo.class));
        verify(applicationBaseService, times(0)).update(any(ApplicationBase.class));
    }

    @Test
    void shouldGetScreenshotsInfo() {
        List<UserFile> result = this.appScreenshotsController.getScreenshotsInfo(appWithLogo.getId());
        assertEquals(2, result.size());
    }

    @Test
    void shouldUploadScreenshot() {
        MultipartFile mf = mock(MultipartFile.class);
        FileInfo newScreenshot = new FileInfo("newScreenshot", "image/png");
        when(fileStorageService.store(mf)).thenReturn(newScreenshot);

        FileInfoView fiv = this.appScreenshotsController.uploadScreenshot(app.getId(), mf);

        assertEquals(newScreenshot.getFilename(), fiv.getFilename());
        verify(applicationBaseService, times(1)).update(any(ApplicationBase.class));
    }

    @Test
    void shouldDeleteAllScreenshots() {
        this.appScreenshotsController.deleteScreenshots(appWithLogo.getId());

        verify(fileStorageService, times(2)).remove(any(FileInfo.class));
        assertEquals(0L, appWithLogo.getScreenshots().size());
    }

    @Test
    void shouldDeleteSingleScreenshot() {
        this.appScreenshotsController.deleteScreenshot(appWithLogo.getId(), 1L);

        verify(fileStorageService, times(1)).remove(any(FileInfo.class));
        verify(applicationBaseService, times(1)).update(any(ApplicationBase.class));
        assertEquals(1, appWithLogo.getScreenshots().size());
    }

    @Test
    void shouldThrowExceptionWhenScreenshotToDeleteNotFound() {
        MissingElementException me = assertThrows(MissingElementException.class, () -> {
            this.appScreenshotsController.deleteScreenshot(app.getId(), 12L);
        });

        assertTrue(me.getMessage().contains("12"));
        assertTrue(me.getMessage().contains(String.valueOf(app.getId())));
    }

    @Test
    void shouldThrowExceptionWhenScreenshotNotFound() {
        MissingElementException me = assertThrows(MissingElementException.class, () -> {
            this.appScreenshotsController.getScreenshot(app.getId(), 12L);
        });

        assertTrue(me.getMessage().contains("12"));
        assertTrue(me.getMessage().contains(String.valueOf(app.getId())));
        Long appWithLogoId = appWithLogo.getId();

        me = assertThrows(MissingElementException.class, () -> {
            this.appScreenshotsController.getScreenshot(appWithLogoId, 13L);
        });

        assertTrue(me.getMessage().contains("13"));
        assertTrue(me.getMessage().contains(String.valueOf(appWithLogoId)));
    }
}
