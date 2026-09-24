package net.geant.nmaas.portal.service;

import net.geant.nmaas.api.dto.variables.VariableDto;
import net.geant.nmaas.api.dto.variables.VariableScopeDto;

import java.util.List;
import java.util.Optional;

public interface VariableService {

    /**
     * Creates a new variable. An instance level (global) variable must not already
     * exist with the same name; a domain level variable must not already exist with
     * the same name in that domain.
     */
    VariableDto create(VariableDto dto);

    /**
     * Overwrites an existing variable with new content. This is the only way to
     * change a secret variable.
     */
    VariableDto update(Long id, VariableDto dto);

    void delete(Long id);

    VariableDto get(Long id);

    /**
     * Lists variables defined directly at the given scope (instance level, or within a domain).
     */
    List<VariableDto> list(VariableScopeDto scope, Long domainId);

    /**
     * Lists all variables effective for the given domain: instance level variables
     * overridden by domain level variables with the same name (which take precedence).
     */
    List<VariableDto> listEffective(Long domainId);

    /**
     * Resolves the effective value of a variable for the given domain, applying the
     * precedence rules: a domain level variable shadows an instance level variable
     * with the same name.
     */
    Optional<VariableDto> resolve(String name, Long domainId);

}
