package net.geant.nmaas.portal.api.variables;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.geant.nmaas.api.dto.variables.VariableDto;
import net.geant.nmaas.api.dto.variables.VariableScopeDto;
import net.geant.nmaas.portal.service.VariableService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/${nmaas.api.version:v1}/variables")
@RequiredArgsConstructor
@Tag(name = "Variables", description = "Central variable storage API")
public class VariableController {

    private final VariableService variableService;

    /**
     * Lists variables defined directly at the given scope.
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasPermission(#domainId, 'domain', 'OWNER')")
    public ResponseEntity<List<VariableDto>> list(
            @RequestParam(value = "scope", defaultValue = "GLOBAL") VariableScopeDto scope,
            @RequestParam(value = "domainId", required = false) Long domainId) {
        return ResponseEntity.ok(variableService.list(scope, domainId));
    }

    /**
     * Lists all variables effective for the given domain, with domain level variables
     * taking precedence over instance level variables with the same name.
     */
    @GetMapping("/effective")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasPermission(#domainId, 'domain', 'READ')")
    public ResponseEntity<List<VariableDto>> listEffective(
            @RequestParam(value = "domainId") Long domainId) {
        return ResponseEntity.ok(variableService.listEffective(domainId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<VariableDto> get(@PathVariable("id") Long id) {
        return ResponseEntity.ok(variableService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || (#variable != null && #variable.scope?.name() == 'DOMAIN' && hasPermission(#variable.domainId, 'domain', 'OWNER'))")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Long create(@RequestBody @Valid VariableDto variable) {
        return variableService.create(variable).getId();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || (#variable != null && #variable.scope?.name() == 'DOMAIN' && hasPermission(#variable.domainId, 'domain', 'OWNER'))")
    public ResponseEntity<VariableDto> update(@PathVariable("id") Long id, @RequestBody @Valid VariableDto variable) {
        if (!id.equals(variable.getId())) {
            throw new IllegalArgumentException("Path and body id are not equal");
        }
        return ResponseEntity.ok(variableService.update(id, variable));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public void delete(@PathVariable("id") Long id) {
        variableService.delete(id);
    }

    /**
     * Resolves the effective value of a single variable for the given domain.
     */
    @GetMapping("/resolve")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasPermission(#domainId, 'domain', 'READ')")
    public ResponseEntity<VariableDto> resolve(
            @RequestParam("name") String name,
            @RequestParam(value = "domainId", required = false) Long domainId) {
        return variableService.resolve(name, domainId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
