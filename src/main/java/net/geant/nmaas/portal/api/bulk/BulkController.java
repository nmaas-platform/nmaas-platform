package net.geant.nmaas.portal.api.bulk;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.domain.BulkDeploymentView;
import net.geant.nmaas.portal.api.domain.BulkDeploymentViewS;
import net.geant.nmaas.portal.api.domain.UserViewMinimal;
import net.geant.nmaas.portal.persistent.entity.BulkDeployment;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.BulkCsvProcessor;
import net.geant.nmaas.portal.service.BulkDomainService;
import net.geant.nmaas.portal.service.BulkHistoryService;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
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
import java.util.List;
import java.util.stream.Collectors;

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
    public ResponseEntity<BulkDeploymentViewS> uploadDomains(@NotNull Principal principal,
                                                            @RequestParam("file") MultipartFile file) {
        if (bulkCsvProcessor.isCSVFormat(file)) {
            try {
                List<CsvBean> csvDomains = bulkCsvProcessor.process(file, CsvDomain.class);
                User userFromDb = userService.findByUsername(principal.getName()).orElseThrow();
                UserViewMinimal user = modelMapper.map(userFromDb, UserViewMinimal.class);
                List<BulkDeploymentEntryView> entries = bulkDomainService.handleBulkCreation(csvDomains);
                return ResponseEntity.ok(
                        modelMapper.map(
                                bulkHistoryService.createFromEntries(entries, user),
                                BulkDeploymentViewS.class)
                );
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
    public ResponseEntity<List<BulkDeploymentEntryView>> uploadApplications(@RequestParam("file") MultipartFile file) {
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
    public ResponseEntity<List<BulkDeploymentViewS>> getAllDeploymentRecords() {
        return ResponseEntity.ok(bulkHistoryService.findAll()
                .stream()
                .map(bulk -> modelMapper.map(bulk, BulkDeploymentViewS.class))
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<BulkDeploymentView> getDeploymentRecord(@PathVariable Long id) {
        BulkDeployment bulk = bulkHistoryService.find(id);
        BulkDeploymentView bulkView = modelMapper.map(bulk, BulkDeploymentView.class);
        bulkView.setCreator(getUserView(bulk.getCreatorId()));
        return ResponseEntity.ok(bulkView);
    }

    @GetMapping("/domains")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<List<BulkDeploymentViewS>> getDomainDeploymentRecords() {
        return ResponseEntity.ok(bulkHistoryService.findAllByType(BulkType.DOMAIN)
                .stream()
                .map(bulk -> {
                    BulkDeploymentViewS bulkView = modelMapper.map(bulk, BulkDeploymentViewS.class);
                    bulkView.setCreator(getUserView(bulk.getCreatorId()));
                    return bulkView;
                })
                .collect(Collectors.toList()));
    }

    @GetMapping("/apps")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<List<BulkDeploymentViewS>> getAppDeploymentRecords() {
        return ResponseEntity.ok(bulkHistoryService.findAllByType(BulkType.APPLICATION)
                .stream()
                .map(bulk -> modelMapper.map(bulk, BulkDeploymentViewS.class))
                .collect(Collectors.toList()));
    }

    private UserViewMinimal getUserView(Long id) {
        User user = userService.findById(id)
                .orElseThrow();
        return modelMapper.map(user, UserViewMinimal.class);
    }

}
