package net.geant.nmaas.portal.api.bulk;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.domain.UserViewMinimal;
import net.geant.nmaas.portal.persistent.entity.BulkDeployment;
import net.geant.nmaas.portal.persistent.entity.BulkDeploymentEntry;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.BulkDeploymentRepository;
import net.geant.nmaas.portal.service.BulkApplicationService;
import net.geant.nmaas.portal.service.BulkCsvProcessor;
import net.geant.nmaas.portal.service.BulkDomainService;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/bulks")
public class BulkController {

    private final BulkCsvProcessor bulkCsvProcessor;

    private final BulkDomainService bulkDomainService;
    private final BulkApplicationService bulkApplicationService;

    private final BulkDeploymentRepository bulkDeploymentRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @PostMapping("/domains")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<BulkDeploymentViewS> uploadDomains(@NotNull Principal principal, @RequestParam("file") MultipartFile file) {
        log.info("Processing new bulk domain deployment request");
        if (bulkCsvProcessor.isCSVFormat(file)) {
            try {
                List<CsvDomain> csvDomains = bulkCsvProcessor.processDomainSpecs(file);
                User userFromDb = userService.findByUsername(principal.getName()).orElseThrow();
                UserViewMinimal user = modelMapper.map(userFromDb, UserViewMinimal.class);
                return ResponseEntity.ok(bulkDomainService.handleBulkCreation(csvDomains, user));
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
    public ResponseEntity<BulkDeploymentViewS> uploadApplications(
            @NotNull Principal principal,
            @RequestParam("appName") String applicationName,
            @RequestParam("file") MultipartFile file) {
        log.info("Processing new bulk application deployment request");
        if (bulkCsvProcessor.isCSVFormat(file)) {
            try {
                List<CsvApplication> csvApplications = bulkCsvProcessor.processApplicationSpecs(file);
                User userFromDb = userService.findByUsername(principal.getName()).orElseThrow();
                UserViewMinimal user = modelMapper.map(userFromDb, UserViewMinimal.class);
                return ResponseEntity.ok(bulkApplicationService.handleBulkDeployment(applicationName, csvApplications, user));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            log.warn("Incorrect input file format");
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<List<BulkDeploymentViewS>> getAllDeploymentRecords() {
        return ResponseEntity.ok(mapToView(bulkDeploymentRepository.findAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<BulkDeploymentView> getDeploymentRecord(@PathVariable Long id) {
        BulkDeployment bulk = bulkDeploymentRepository.findById(id).orElseThrow();
        BulkDeploymentView bulkView = modelMapper.map(bulk, BulkDeploymentView.class);
        bulkView.setCreator(getUserView(bulk.getCreatorId()));
        mapDetails(bulk, bulkView);
        return ResponseEntity.ok(bulkView);
    }

    @GetMapping(value = "/app/csv/{id}", produces = "text/csv")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<InputStreamResource> getDeploymentDetailsInCSV(@PathVariable Long id) {
        BulkDeployment bulk = bulkDeploymentRepository.findById(id).orElseThrow();
        BulkDeploymentView bulkView = modelMapper.map(bulk, BulkDeploymentView.class);
        bulkView.setCreator(getUserView(bulk.getCreatorId()));
        mapDetails(bulk, bulkView);
        List<BulkAppDetails> list = this.bulkApplicationService.getAppsBulkDetails(bulkView);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=csvDetails");
        headers.set(HttpHeaders.CONTENT_TYPE, "text/csv");

        InputStreamResource inputStreamResource = bulkApplicationService.getInputStreamAppBulkDetails(list);

        return ResponseEntity.ok().headers(headers).body(inputStreamResource);
    }

    @GetMapping("/domains")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<List<BulkDeploymentViewS>> getDomainDeploymentRecords() {
        return ResponseEntity.ok(mapToView(bulkDeploymentRepository.findByType(BulkType.DOMAIN)));
    }

    @GetMapping("/apps")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<List<BulkDeploymentViewS>> getAppDeploymentRecords() {
        return ResponseEntity.ok(mapToView(bulkDeploymentRepository.findByType(BulkType.APPLICATION)));
    }

    private List<BulkDeploymentViewS> mapToView(List<BulkDeployment> deployments) {
        return deployments.stream()
                .map(bulk -> {
                    BulkDeploymentViewS bulkView = modelMapper.map(bulk, BulkDeploymentViewS.class);
                    bulkView.setCreator(getUserView(bulk.getCreatorId()));
                    mapDetails(bulk, bulkView);
                    return bulkView;
                })
                .collect(Collectors.toList());
    }

    private void mapDetails(BulkDeployment deployment, BulkDeploymentViewS view) {
        if (deployment.getType().equals(BulkType.APPLICATION)) {
            Map<String, String> details = new HashMap<>();
            if (!deployment.getEntries().isEmpty()) {
                details.put(BulkDeploymentViewS.BULK_DETAIL_KEY_APP_INSTANCE_NO, String.valueOf(deployment.getEntries().size()));
                BulkDeploymentEntry entry = deployment.getEntries().get(0);
                if (entry.getDetails().containsKey(BulkDeploymentEntryView.BULK_ENTRY_DETAIL_KEY_APP_ID)) {
                    details.put(BulkDeploymentViewS.BULK_DETAIL_KEY_APP_ID, entry.getDetails().get(BulkDeploymentEntryView.BULK_ENTRY_DETAIL_KEY_APP_ID));
                }
                if (entry.getDetails().containsKey(BulkDeploymentEntryView.BULK_ENTRY_DETAIL_KEY_APP_NAME)) {
                    details.put(BulkDeploymentViewS.BULK_DETAIL_KEY_APP_NAME, entry.getDetails().get(BulkDeploymentEntryView.BULK_ENTRY_DETAIL_KEY_APP_NAME));
                }
                view.setDetails(details);
            }
        }
    }

    private UserViewMinimal getUserView(Long id) {
        User user = userService.findById(id)
                .orElseThrow();
        return modelMapper.map(user, UserViewMinimal.class);
    }

}
