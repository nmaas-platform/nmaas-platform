package net.geant.nmaas.portal.api.bulk;

import net.geant.nmaas.portal.api.domain.BulkDeploymentViewS;
import net.geant.nmaas.portal.persistent.entity.BulkDeployment;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.BulkCsvProcessor;
import net.geant.nmaas.portal.service.BulkDomainService;
import net.geant.nmaas.portal.service.BulkHistoryService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
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
    private final ModelMapper modelMapper = new ModelMapper();
    private final BulkHistoryService bulkHistoryService = mock(BulkHistoryService.class);
    private final UserService userService = mock(UserService.class);
    private final Principal principalMock = mock(Principal.class);

    private BulkController bulkController;

    @BeforeEach
    void setup() {
        this.bulkController = new BulkController(bulkCsvProcessor, bulkDomainService, bulkHistoryService,
                userService, modelMapper);
    }

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
        when(bulkHistoryService.createFromEntries(any(), any())).thenReturn(new BulkDeployment());
        when(principalMock.getName()).thenReturn("user");

        ResponseEntity<BulkDeploymentViewS> response = bulkController.uploadDomains(principalMock, file);

        verify(bulkCsvProcessor).process(any(), any());
        verify(bulkDomainService).handleBulkCreation(any());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldHandleIncorrectFileFormatForBulkApplicationRequest() {
        MultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", "invalid content".getBytes());
        when(bulkCsvProcessor.isCSVFormat(any())).thenReturn(false);
        ResponseEntity<List<BulkDeploymentEntryView>> response = bulkController.uploadApplications(file);
        verifyNoInteractions(bulkDomainService);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldProcessBulkApplicationRequest() throws IOException {
        MultipartFile file = new MockMultipartFile("test.csv", "test.csv", "text/csv", "content".getBytes());
        when(bulkCsvProcessor.isCSVFormat(any())).thenReturn(true);
        ResponseEntity<List<BulkDeploymentEntryView>> response = bulkController.uploadApplications(file);
        verify(bulkCsvProcessor).process(any(), any());
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

}
