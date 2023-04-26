package net.geant.nmaas.portal.api.bulk;

import net.geant.nmaas.portal.service.BulkCsvProcessor;
import net.geant.nmaas.portal.service.BulkDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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

    private BulkController bulkController;

    @BeforeEach
    void setup() {
        this.bulkController = new BulkController(bulkCsvProcessor, bulkDomainService);
    }

    @Test
    void shouldHandleIncorrectFileFormatForBulkDomainRequest() {
        MultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", "invalid content".getBytes());
        when(bulkCsvProcessor.isCSVFormat(any())).thenReturn(false);
        ResponseEntity<List<CsvProcessorResponse>> response = bulkController.uploadDomains(file);
        verifyNoInteractions(bulkDomainService);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldProcessBulkDomainRequest() throws IOException {
        MultipartFile file = new MockMultipartFile("test.csv", "test.csv", "text/csv", "content".getBytes());
        when(bulkCsvProcessor.isCSVFormat(any())).thenReturn(true);
        ResponseEntity<List<CsvProcessorResponse>> response = bulkController.uploadDomains(file);
        verify(bulkCsvProcessor).process(any(), any());
        verify(bulkDomainService).handleBulkCreation(any());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldHandleIncorrectFileFormatForBulkApplicationRequest() {
        MultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", "invalid content".getBytes());
        when(bulkCsvProcessor.isCSVFormat(any())).thenReturn(false);
        ResponseEntity<List<CsvProcessorResponse>> response = bulkController.uploadApplications(file);
        verifyNoInteractions(bulkDomainService);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldProcessBulkApplicationRequest() throws IOException {
        MultipartFile file = new MockMultipartFile("test.csv", "test.csv", "text/csv", "content".getBytes());
        when(bulkCsvProcessor.isCSVFormat(any())).thenReturn(true);
        ResponseEntity<List<CsvProcessorResponse>> response = bulkController.uploadApplications(file);
        verify(bulkCsvProcessor).process(any(), any());
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

}
