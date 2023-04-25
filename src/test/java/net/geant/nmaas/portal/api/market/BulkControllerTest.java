package net.geant.nmaas.portal.api.market;

import net.geant.nmaas.portal.api.bulk.BulkController;
import net.geant.nmaas.portal.api.bulk.CsvProcessorResponse;
import net.geant.nmaas.portal.service.BulkCsvProcessor;
import net.geant.nmaas.portal.service.BulkDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class BulkControllerTest {

    private ModelMapper modelMapper = new ModelMapper();

    private BulkCsvProcessor bulkCsvProcessor = mock(BulkCsvProcessor.class);

    private BulkDomainService bulkDomainService = mock(BulkDomainService.class);

    private BulkController bulkController;

    @BeforeEach
    void setup() {
        this.bulkController = new BulkController(bulkCsvProcessor, bulkDomainService);
    }

    @Test
    void shouldHandleMissingFile() {
        ResponseEntity<List<CsvProcessorResponse>> response = bulkController.uploadDomains(null);
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldHandleBadExtension() {
        MultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", "invalid content".getBytes());
        ResponseEntity<List<CsvProcessorResponse>> response = bulkController.uploadDomains(file);
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }
}
