package net.geant.nmaas.portal.api.bulk;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.service.BulkCsvProcessor;
import net.geant.nmaas.portal.service.BulkDomainService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/bulks")
public class BulkController {

    private final BulkCsvProcessor bulkCsvProcessor;
    private final BulkDomainService bulkDomainService;

    @PostMapping("/domains")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<List<CsvProcessorResponse>> uploadDomains(@RequestParam("file") MultipartFile file) {
        if (bulkCsvProcessor.isCSVFormat(file)) {
            try {
                List<CsvBean> csvDomains = bulkCsvProcessor.process(file, CsvDomain.class);
                return ResponseEntity.ok(bulkDomainService.handleBulkCreation(csvDomains));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            log.warn("Incorrect input file format");
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/apps")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<List<CsvProcessorResponse>> uploadApplications(@RequestParam("file") MultipartFile file) {
        if (bulkCsvProcessor.isCSVFormat(file)) {
            try {
                List<CsvBean> csvApplications = bulkCsvProcessor.process(file, CsvApplication.class);
                // TODO trigger bulk application deployment
                return ResponseEntity.noContent().build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            log.warn("Incorrect input file format");
            return ResponseEntity.badRequest().build();
        }
    }

}
