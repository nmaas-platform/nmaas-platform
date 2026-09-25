package net.geant.nmaas.portal.api.apps;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.geant.nmaas.portal.dto.BundleBasicDto;
import net.geant.nmaas.portal.dto.BundleDto;
import net.geant.nmaas.portal.service.BundleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/${nmaas.api.version:v1}/bundles")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Applications Bundles", description = "Applications bundles")
public class BundleController {

    private final BundleService bundleService;

    @PostMapping
    public ResponseEntity<BundleDto> create(@Valid @RequestBody BundleBasicDto request) {
        BundleDto bundle = bundleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(bundle);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BundleDto> update(@PathVariable Long id, @Valid @RequestBody BundleBasicDto request) {
        BundleDto bundle = bundleService.update(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(bundle);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BundleDto> getOne(@PathVariable Long id) {
        BundleDto bundle = bundleService.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(bundle);
    }

    @GetMapping
    public Page<BundleBasicDto> getAll(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return bundleService.findAll(pageable);
    }

    @GetMapping("application/{applicationId}")
    public Page<BundleBasicDto> getAllByApplication(@PathVariable Long applicationId,
                                                    @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return bundleService.findByApplication(applicationId, pageable);
    }

    @GetMapping("application/{applicationId}/raw")
    public ResponseEntity<List<BundleDto>> getAllByApplication(@PathVariable Long applicationId) {
        List<BundleDto> response = bundleService.findByApplication(applicationId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(params = "name")
    public Page<BundleBasicDto> searchByName(@RequestParam String name,
                                             @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return bundleService.findByName(name, pageable);
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> isApplicationInBundle(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(bundleService.isApplicationInBundle(id));
    }

}
