package net.geant.nmaas.portal.api.bulk;

import net.geant.nmaas.portal.api.domain.UserViewMinimal;
import net.geant.nmaas.portal.persistent.entity.BulkDeployment;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.BulkDeploymentRepository;
import net.geant.nmaas.portal.service.BulkApplicationService;
import net.geant.nmaas.portal.service.BulkCsvProcessor;
import net.geant.nmaas.portal.service.BulkDomainService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class BulkControllerTest {

    private final BulkCsvProcessor bulkCsvProcessor = mock(BulkCsvProcessor.class);
    private final BulkDomainService bulkDomainService = mock(BulkDomainService.class);
    private final BulkApplicationService bulkApplicationService = mock(BulkApplicationService.class);

    private final BulkDeploymentRepository bulkDeploymentRepository = mock(BulkDeploymentRepository.class);
    private final UserService userService = mock(UserService.class);
    private final Principal principalMock = mock(Principal.class);

    private final ModelMapper modelMapper = new ModelMapper();

    private final BulkController bulkController = new BulkController(bulkCsvProcessor, bulkDomainService, bulkApplicationService,
            bulkDeploymentRepository, userService, modelMapper);

    @Test
    void shouldHandleIncorrectFileFormatForBulkDomainRequest() {
        MultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", "invalid content".getBytes());
        when(bulkCsvProcessor.isCSVFormat(any())).thenReturn(false);

        ResponseEntity<BulkDeploymentViewS> response = bulkController.uploadDomains(principalMock, file);

        verifyNoInteractions(bulkDomainService);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldProcessBulkDomainRequest() throws IOException {
        MultipartFile file = new MockMultipartFile("test.csv", "test.csv", "text/csv", "content".getBytes());
        when(bulkCsvProcessor.isCSVFormat(any())).thenReturn(true);
        when(userService.findByUsername("user")).thenReturn(Optional.of(new User("user")));
        when(principalMock.getName()).thenReturn("user");

        ResponseEntity<BulkDeploymentViewS> response = bulkController.uploadDomains(principalMock, file);

        verify(bulkCsvProcessor).processDomainSpecs(any());
        verify(bulkDomainService).handleBulkCreation(any(), any());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldHandleIncorrectFileFormatForBulkApplicationRequest() {
        MultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", "invalid content".getBytes());
        when(bulkCsvProcessor.isCSVFormat(any())).thenReturn(false);
        when(principalMock.getName()).thenReturn("user");

        ResponseEntity<BulkDeploymentViewS> response = bulkController.uploadApplications(principalMock, "applicationName", file);

        verifyNoInteractions(bulkApplicationService);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldProcessBulkApplicationRequest() throws IOException {
        MultipartFile file = new MockMultipartFile("test.csv", "test.csv", "text/csv", "content".getBytes());
        when(bulkCsvProcessor.isCSVFormat(any())).thenReturn(true);
        when(principalMock.getName()).thenReturn("user");
        when(userService.findByUsername("user")).thenReturn(Optional.of(new User("user")));

        ResponseEntity<BulkDeploymentViewS> response = bulkController.uploadApplications(principalMock, "applicationName", file);

        verify(bulkCsvProcessor).processApplicationSpecs(any());
        verify(bulkApplicationService).handleBulkDeployment(any(), any(), any());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldGetDomainBulksAsVLManager() {
        when(principalMock.getName()).thenReturn("user");
        User user = new User("user");
        user.setId(10L);
        when(userService.findByUsername("user")).thenReturn(Optional.of(user));
        when(userService.findById(10L)).thenReturn(Optional.of(user));
        List<BulkDeployment> bulks = new ArrayList<>();
        BulkDeployment viewS = new BulkDeployment();
        viewS.setType(BulkType.DOMAIN);
        viewS.setCreatorId(10L);
        bulks.add(viewS);
        when(bulkDeploymentRepository.findByType(BulkType.DOMAIN)).thenReturn(List.of(viewS));
        assertEquals(1, Objects.requireNonNull(bulkController.getDomainDeploymentRecordsRestrictedToOwner(principalMock).getBody()).size());
    }


    @Test
    void shouldGetAppBulksAsVLManager() {
        when(principalMock.getName()).thenReturn("user");
        User user = new User("user");
        user.setId(10L);
        User user2 = new User("user2");
        user2.setId(20L);
        when(userService.findByUsername("user")).thenReturn(Optional.of(user));
        when(userService.findByUsername("user2")).thenReturn(Optional.of(user2));
        when(userService.findById(10L)).thenReturn(Optional.of(user));
        when(userService.findById(20L)).thenReturn(Optional.of(user2));
        List<BulkDeployment> bulks = new ArrayList<>();
        BulkDeployment base1 = new BulkDeployment();
        base1.setType(BulkType.DOMAIN);
        base1.setCreatorId(10L);
        bulks.add(base1);
        BulkDeployment base2 = new BulkDeployment();
        base2.setType(BulkType.DOMAIN);
        base2.setCreatorId(20L);
        bulks.add(base2);
        when(bulkDeploymentRepository.findByType(BulkType.APPLICATION)).thenReturn(bulks);
        assertEquals(1, Objects.requireNonNull(bulkController.getAppDeploymentRecordsRestrictedToOwner(principalMock).getBody()).size());
    }
}
