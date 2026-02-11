package net.geant.nmaas.portal.api.domains;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.geant.nmaas.api.dto.Id;
import net.geant.nmaas.api.dto.ResourcesLimitDto;
import net.geant.nmaas.api.dto.ResourcesLimitUpdateDto;
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

import java.util.List;

@RestController
@RequestMapping("/api/resources-limits")
@RequiredArgsConstructor
@Tag(name = "ResourcesLimit", description = "ResourcesLimits management API")
public class ResourcesLimitController {

    private final ResourcesLimitService resourcesLimitService;

    @PostMapping("/global")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @Transactional
    public ResponseEntity<Void> setGlobalResourcesLimit(@RequestBody @Valid ResourcesLimitDto resourcesLimit) {
        resourcesLimitService.setGlobalResourcesLimit(resourcesLimit);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/global")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @Transactional
    public ResponseEntity<ResourcesLimitDto> getGlobalResourcesLimit() {
        return ResponseEntity.ok(resourcesLimitService.getGlobalResourcesLimit());
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @Transactional
    public ResponseEntity<Id> createResourcesLimit(@RequestBody @Valid ResourcesLimitDto resourcesLimit) {
        resourcesLimit = resourcesLimitService.create(resourcesLimit);
        return ResponseEntity.ok(new Id(resourcesLimit.id()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @Transactional
    public ResponseEntity<Id> updateResourcesLimit(@PathVariable Long id, @RequestBody @Valid ResourcesLimitUpdateDto resourcesLimit) {
        if (!id.equals(resourcesLimit.id())) {
            throw new ProcessingException("Path and body id are not equal");
        }
        resourcesLimitService.update(resourcesLimit);
        return ResponseEntity.ok(new Id(resourcesLimit.id()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @Transactional
    public void deleteResourcesLimit(@PathVariable Long id) {
        resourcesLimitService.delete(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @Transactional
    public ResponseEntity<ResourcesLimitDto> getResourcesLimit(@PathVariable Long id) {
        return ResponseEntity.ok(resourcesLimitService.getResourcesLimit(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @Transactional
    public ResponseEntity<List<ResourcesLimitDto>> getAllResourcesLimits() {
        return ResponseEntity.ok(resourcesLimitService.getAllResourcesLimits());
    }

    @GetMapping("/domain/{domainId}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasPermission(#domainId, 'domain', 'OWNER')")
    @Transactional
    public ResponseEntity<ResourcesLimitDto> getDomainResourceLimit(@PathVariable Long domainId) {
        return ResponseEntity.ok(resourcesLimitService.getDomainResourceLimit(domainId));
    }

    @GetMapping("/group/{groupId}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_GROUP_MANAGER')")
    @Transactional
    public ResponseEntity<ResourcesLimitDto> getGroupResourceLimit(@PathVariable Long groupId) {
        return ResponseEntity.ok(resourcesLimitService.getGroupResourceLimit(groupId));
    }

}
