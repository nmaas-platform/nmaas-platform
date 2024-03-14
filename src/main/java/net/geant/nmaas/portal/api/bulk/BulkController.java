package net.geant.nmaas.portal.api.bulk;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.domain.UserViewMinimal;
import net.geant.nmaas.portal.api.exception.MissingElementException;
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
import org.springframework.web.bind.annotation.DeleteMapping;
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
import java.util.Optional;
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
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_VL_MANAGER')")
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
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_VL_MANAGER')")
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
        return ResponseEntity.ok(mapToViewList(bulkDeploymentRepository.findAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_VL_MANAGER')")
    public ResponseEntity<BulkDeploymentView> getDeploymentRecord(@PathVariable Long id) {
        BulkDeployment bulk = bulkDeploymentRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(mapToView(bulk, BulkDeploymentView.class));
    }

    @GetMapping(value = "/app/csv/{id}", produces = "text/csv")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_VL_MANAGER')")
    public ResponseEntity<InputStreamResource> getDeploymentDetailsInCSV(@PathVariable Long id) {
        log.info("Processing bulk application deployment details request");
        BulkDeployment bulk = bulkDeploymentRepository.findById(id).orElseThrow();
        BulkDeploymentView bulkView = modelMapper.map(bulk, BulkDeploymentView.class);
        bulkView.setCreator(getUserView(bulk.getCreatorId()));
        mapDetails(bulk, bulkView);
        List<BulkAppDetails> details = bulkApplicationService.getAppsBulkDetails(bulkView);
        InputStreamResource inputStreamResource = bulkApplicationService.getInputStreamAppBulkDetails(details);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=BulkDetailsCsv");
        headers.set(HttpHeaders.CONTENT_TYPE, "text/csv");
        return ResponseEntity.ok().headers(headers).body(inputStreamResource);
    }

    @GetMapping("/domains")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<List<BulkDeploymentViewS>> getDomainDeploymentRecords() {
        return ResponseEntity.ok(mapToViewList(bulkDeploymentRepository.findByType(BulkType.DOMAIN)));
    }

    @GetMapping("/domains/vl")
    @PreAuthorize("hasRole('ROLE_VL_MANAGER')")
    public ResponseEntity<List<BulkDeploymentViewS>> getDomainDeploymentRecordsRestrictedToOwner(Principal principal) {
        User user = this.userService.findByUsername(principal.getName()).orElseThrow(() -> new MissingElementException("Missing user " + principal.getName()));

        return ResponseEntity.ok(mapToViewList(bulkDeploymentRepository.findByType(BulkType.DOMAIN)).stream()
                .filter(bulk -> bulk.getCreator().getId().equals(user.getId())).collect(Collectors.toList()));
    }

    @GetMapping("/apps")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<List<BulkDeploymentViewS>> getAppDeploymentRecords() {
        return ResponseEntity.ok(mapToViewList(bulkDeploymentRepository.findByType(BulkType.APPLICATION)));
    }

    @GetMapping("/apps/vl")
    @PreAuthorize("hasRole('ROLE_VL_MANAGER')")
    public ResponseEntity<List<BulkDeploymentViewS>> getAppDeploymentRecordsRestrictedToOwner(Principal principal) {
        User user = this.userService.findByUsername(principal.getName()).orElseThrow(() -> new MissingElementException("Missing user " + principal.getName()));

        return ResponseEntity.ok(mapToViewList(bulkDeploymentRepository.findByType(BulkType.APPLICATION)).stream()
                .filter(bulk -> bulk.getCreator().getId().equals(user.getId())).collect(Collectors.toList()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<Void> removeBulkDeployment(
            @PathVariable Long id,
            @RequestParam(name = "removeAll") boolean removeApps
    ) {

        Optional<BulkDeployment> bulk = this.bulkDeploymentRepository.findById(id);
        if (bulk.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (removeApps) {
            bulkApplicationService.deleteAppInstancesFromBulk(mapToView(bulk.get(), BulkDeploymentView.class));
        }
        bulkDeploymentRepository.delete(bulk.get());
        return ResponseEntity.ok().build();
    }

    private List<BulkDeploymentViewS> mapToViewList(List<BulkDeployment> deployments) {
        return deployments.stream()
                .map(bulk -> mapToView(bulk, BulkDeploymentViewS.class))
                .collect(Collectors.toList());
    }

    private <T extends BulkDeploymentViewS> T mapToView(BulkDeployment bulk, Class<T> viewType) {
        T bulkView = modelMapper.map(bulk, viewType);
        bulkView.setCreator(getUserView(bulk.getCreatorId()));
        mapDetails(bulk, bulkView);
        return bulkView;
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
