package net.geant.nmaas.portal.api.bulk;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.domain.UserViewMinimal;
import net.geant.nmaas.portal.persistent.entity.BulkDeployment;
import net.geant.nmaas.portal.persistent.entity.CsvProcessorResponse;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.BulkCsvProcessor;
import net.geant.nmaas.portal.service.BulkHistoryService;
import net.geant.nmaas.portal.service.BulkDomainService;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/bulks")
public class BulkController {

    private final BulkCsvProcessor bulkCsvProcessor;
    private final BulkDomainService bulkDomainService;
    private final BulkHistoryService bulkHistoryService;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @PostMapping("/domains")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<List<CsvProcessorResponse>> uploadDomains(@NotNull Principal principal,
                                                                        @RequestParam("file") MultipartFile file) {
        if (bulkCsvProcessor.isCSVFormat(file)) {
            try {
                List<CsvBean> csvDomains = bulkCsvProcessor.process(file, CsvDomain.class);
                User user = userService.findByUsername(principal.getName())
                        .orElseThrow();
                UserViewMinimal userMinimal = modelMapper.map(user, UserViewMinimal.class);
                List<CsvProcessorResponse> csvResponses = bulkDomainService.handleBulkCreation(csvDomains);
                bulkHistoryService.createEntityFromCsvResponse(csvResponses, userMinimal);
                return ResponseEntity.ok(csvResponses);
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

    @GetMapping()
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<List<BulkDeployment>> getAllDeploymentRecords() {
        return ResponseEntity.ok(bulkHistoryService.findAll());
    }

    @GetMapping("/domains")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<List<BulkDeployment>> getDomainDeploymentRecords() {
        return ResponseEntity.ok(bulkHistoryService.findAllByType(BulkType.DOMAIN));
    }

    @GetMapping("/domains/{id}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<BulkDeployment> getDomainDeploymentRecord(@PathVariable Long id) {
        return ResponseEntity.ok(bulkHistoryService.find(id));
    }

    @GetMapping("/apps")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<List<BulkDeployment>> getAppDeploymentRecords() {
        return ResponseEntity.ok(bulkHistoryService.findAllByType(BulkType.APPLICATION));
    }

    @GetMapping("/apps/{id}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<BulkDeployment> getAppDeploymentRecord(@PathVariable Long id) {
        return ResponseEntity.ok(bulkHistoryService.find(id));
    }

}
