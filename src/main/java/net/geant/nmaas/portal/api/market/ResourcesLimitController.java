package net.geant.nmaas.portal.api.market;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.geant.nmaas.portal.api.domain.Id;
import net.geant.nmaas.portal.api.domain.ResourcesLimitDto;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.service.ResourcesLimitService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequestMapping("/api/resources-limits")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
@Tag(name = "ResourcesLimit", description = "ResourcesLimits management API")
public class ResourcesLimitController {

    private final ResourcesLimitService resourcesLimitService;

    @PostMapping
    @Transactional
    public ResponseEntity<Id> createResourcesLimit(@RequestBody @Valid ResourcesLimitDto resourcesLimit) {
        resourcesLimit = resourcesLimitService.create(resourcesLimit);
        return ResponseEntity.ok(new Id(resourcesLimit.getId()));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Id> updateResourcesLimit(@PathVariable Long id, @RequestBody @Valid ResourcesLimitDto resourcesLimit) {
        if (!id.equals(resourcesLimit.getId())) {
            throw new ProcessingException("Path and body id are not equal");
        }
        resourcesLimitService.update(resourcesLimit);
        return ResponseEntity.ok(new Id(resourcesLimit.getId()));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void deleteResourcesLimit(@PathVariable Long id) {
        resourcesLimitService.delete(id);
    }

    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<ResourcesLimitDto> getResourcesLimit(@PathVariable Long id) {
        return ResponseEntity.ok(resourcesLimitService.getResourcesLimit(id));
    }

    @GetMapping
    @Transactional
    public ResponseEntity<List<ResourcesLimitDto>> getAllResourcesLimits() {
        return ResponseEntity.ok(resourcesLimitService.getAllResourcesLimits());
    }

}
