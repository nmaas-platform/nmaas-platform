package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.api.dto.variables.VariableDto;
import net.geant.nmaas.api.dto.variables.VariableScopeDto;
import net.geant.nmaas.api.dto.variables.VariableTypeDto;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.persistence.entity.Variable;
import net.geant.nmaas.portal.persistence.entity.VariableType;
import net.geant.nmaas.portal.persistence.repositories.DomainRepository;
import net.geant.nmaas.portal.persistence.repositories.VariableRepository;
import net.geant.nmaas.portal.service.VariableService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VariableServiceImpl implements VariableService {

    private static final String VARIABLE_NOT_FOUND = "Variable not found";
    private static final String VARIABLE_ALREADY_EXISTS = "Variable with this name already exists in this scope";

    private final VariableRepository variableRepository;
    private final DomainRepository domainRepository;

    @Override
    @Transactional
    public VariableDto create(VariableDto dto) {
        validateScope(dto);
        if (existsInScope(dto)) {
            throw new ObjectAlreadyExistsException(VARIABLE_ALREADY_EXISTS);
        }
        Variable entity = new Variable();
        mapToEntity(dto, entity);
        // initially, the last modification timestamp equals the creation time
        entity.setLastModified(System.currentTimeMillis());
        entity = variableRepository.save(entity);
        log.info("Created variable {} of type {} in scope {}", entity.getName(), entity.getType(), describeScope(entity));
        return toDto(entity, true);
    }

    @Override
    @Transactional
    public VariableDto update(Long id, VariableDto dto) {
        validateScope(dto);
        Variable entity = variableRepository.findById(id)
                .orElseThrow(() -> new MissingElementException(VARIABLE_NOT_FOUND));
        // a secret variable can only be overwritten with new content, never converted to another type
        if (entity.getType() == VariableType.SECRET
                && dto.getType() != null
                && dto.getType() != VariableTypeDto.SECRET) {
            throw new ProcessingException("Secret variable cannot be converted to another type");
        }
        String storedValue = entity.getValue();
        // a null value in the request keeps the stored value (e.g. metadata-only update)
        mapToEntity(dto, entity);
        // refresh the timestamp only when the value was actually overwritten with new content
        if (dto.getValue() != null && !dto.getValue().equals(storedValue)) {
            entity.setLastModified(System.currentTimeMillis());
        }
        entity = variableRepository.save(entity);
        log.info("Updated variable {} in scope {}", entity.getName(), describeScope(entity));
        return toDto(entity, true);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Variable entity = variableRepository.findById(id)
                .orElseThrow(() -> new MissingElementException(VARIABLE_NOT_FOUND));
        variableRepository.delete(entity);
        log.info("Deleted variable {} in scope {}", entity.getName(), describeScope(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public VariableDto get(Long id) {
        Variable entity = variableRepository.findById(id)
                .orElseThrow(() -> new MissingElementException(VARIABLE_NOT_FOUND));
        return toDto(entity, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VariableDto> list(VariableScopeDto scope, Long domainId) {
        List<Variable> entities;
        if (scope == VariableScopeDto.GLOBAL) {
            entities = variableRepository.findByDomainIsNull();
        } else {
            if (domainId == null) {
                throw new ProcessingException("Domain identifier is required for domain scope");
            }
            entities = variableRepository.findByDomainId(domainId);
        }
        return entities.stream()
                .map(entity -> toDto(entity, true))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VariableDto> listEffective(Long domainId) {
        if (domainId == null) {
            throw new ProcessingException("Domain identifier is required");
        }
        Map<String, Variable> effective = new LinkedHashMap<>();
        // instance level variables first, then domain level variables overwrite
        // the ones with the same name (domain level takes precedence)
        variableRepository.findByDomainIsNull().forEach(variable -> effective.put(variable.getName(), variable));
        variableRepository.findByDomainId(domainId).forEach(variable -> effective.put(variable.getName(), variable));
        return effective.values().stream()
                .map(entity -> toDto(entity, true))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VariableDto> resolve(String name, Long domainId) {
        // domain level variable takes precedence over instance level variable with the same name
        Optional<Variable> domainVariable = domainId != null
                ? variableRepository.findByNameAndDomainId(name, domainId)
                : Optional.empty();
        return domainVariable
                .or(() -> variableRepository.findByNameAndDomainIsNull(name))
                .map(entity -> toDto(entity, true));
    }

    private void validateScope(VariableDto dto) {
        if (dto.getScope() == VariableScopeDto.DOMAIN) {
            if (dto.getDomainId() == null) {
                throw new ProcessingException("Domain is required for domain scope");
            }
            if (!domainRepository.existsById(dto.getDomainId())) {
                throw new MissingElementException("Domain " + dto.getDomainId() + " not found");
            }
        } else if (dto.getScope() == VariableScopeDto.GLOBAL && dto.getDomainId() != null) {
            throw new ProcessingException("Domain cannot be set for instance scope");
        }
    }

    private boolean existsInScope(VariableDto dto) {
        if (dto.getScope() == VariableScopeDto.DOMAIN) {
            return variableRepository.existsByNameAndDomainId(dto.getName(), dto.getDomainId());
        }
        return variableRepository.existsByNameAndDomainIsNull(dto.getName());
    }

    private void mapToEntity(VariableDto dto, Variable entity) {
        entity.setName(dto.getName());
        if (dto.getValue() != null) {
            entity.setValue(dto.getValue());
        }
        if (dto.getType() != null) {
            entity.setType(VariableType.valueOf(dto.getType().name()));
        }
        if (dto.getScope() == VariableScopeDto.DOMAIN) {
            entity.setDomain(domainRepository.getReferenceById(dto.getDomainId()));
        } else {
            entity.setDomain(null);
        }
        if (dto.getValue() == null && entity.getValue() == null) {
            throw new ProcessingException("Variable value is required");
        }
    }

    private VariableDto toDto(Variable entity, boolean maskSecrets) {
        return VariableDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .value(maskSecrets && entity.getType() == VariableType.SECRET
                        ? null
                        : entity.getValue())
                .type(VariableTypeDto.valueOf(entity.getType().name()))
                .scope(entity.isGlobal() ? VariableScopeDto.GLOBAL : VariableScopeDto.DOMAIN)
                .domainId(entity.getDomain() != null ? entity.getDomain().getId() : null)
                .build();
    }

    private String describeScope(Variable entity) {
        return entity.isGlobal() ? "global" : "domain " + entity.getDomain().getId();
    }

}
