package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.api.dto.variables.VariableDto;
import net.geant.nmaas.api.dto.variables.VariableScopeDto;
import net.geant.nmaas.api.dto.variables.VariableTypeDto;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.Variable;
import net.geant.nmaas.portal.persistence.entity.VariableType;
import net.geant.nmaas.portal.persistence.repositories.DomainRepository;
import net.geant.nmaas.portal.persistence.repositories.VariableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VariableServiceImplTest {

    private final VariableRepository variableRepository = mock(VariableRepository.class);
    private final DomainRepository domainRepository = mock(DomainRepository.class);

    private VariableServiceImpl variableService;

    @BeforeEach
    void setUp() {
        variableService = new VariableServiceImpl(variableRepository, domainRepository);
        when(variableRepository.save(any(Variable.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldCreateInstanceLevelVariable() {
        when(variableRepository.existsByNameAndDomainIsNull("proxy.url")).thenReturn(false);

        VariableDto created = variableService.create(VariableDto.builder()
                .name("proxy.url")
                .value("https://proxy.example.com")
                .type(VariableTypeDto.STANDARD)
                .scope(VariableScopeDto.GLOBAL)
                .build());

        assertNotNull(created);
        assertEquals("proxy.url", created.getName());
        assertEquals("https://proxy.example.com", created.getValue());
        assertEquals(VariableScopeDto.GLOBAL, created.getScope());

        ArgumentCaptor<Variable> captor = ArgumentCaptor.forClass(Variable.class);
        verify(variableRepository).save(captor.capture());
        assertNull(captor.getValue().getDomain());
        // initially the last modification timestamp equals the creation time
        assertNotNull(captor.getValue().getLastModified());
    }

    @Test
    void shouldCreateDomainLevelVariable() {
        when(variableRepository.existsByNameAndDomainId("proxy.url", 5L)).thenReturn(false);
        when(domainRepository.existsById(5L)).thenReturn(true);
        when(domainRepository.getReferenceById(5L)).thenReturn(new Domain(5L));

        VariableDto created = variableService.create(VariableDto.builder()
                .name("proxy.url")
                .value("https://domain-proxy.example.com")
                .type(VariableTypeDto.STANDARD)
                .scope(VariableScopeDto.DOMAIN)
                .domainId(5L)
                .build());

        assertEquals(VariableScopeDto.DOMAIN, created.getScope());
        assertEquals(5L, created.getDomainId());

        ArgumentCaptor<Variable> captor = ArgumentCaptor.forClass(Variable.class);
        verify(variableRepository).save(captor.capture());
        assertEquals(5L, captor.getValue().getDomain().getId());
    }

    @Test
    void shouldRejectDuplicateNameInSameScope() {
        when(variableRepository.existsByNameAndDomainIsNull("proxy.url")).thenReturn(true);

        assertThrows(ObjectAlreadyExistsException.class, () -> variableService.create(VariableDto.builder()
                .name("proxy.url")
                .value("value")
                .type(VariableTypeDto.STANDARD)
                .scope(VariableScopeDto.GLOBAL)
                .build()));
    }

    @Test
    void shouldAllowSameNameInDifferentScopes() {
        when(variableRepository.existsByNameAndDomainId("proxy.url", 5L)).thenReturn(false);
        when(domainRepository.existsById(5L)).thenReturn(true);
        when(domainRepository.getReferenceById(5L)).thenReturn(new Domain(5L));

        VariableDto created = variableService.create(VariableDto.builder()
                .name("proxy.url")
                .value("https://domain-proxy.example.com")
                .type(VariableTypeDto.STANDARD)
                .scope(VariableScopeDto.DOMAIN)
                .domainId(5L)
                .build());

        assertNotNull(created);
    }

    @Test
    void shouldMaskSecretValueWhenReading() {
        Variable secret = Variable.builder()
                .id(1L)
                .name("api.password")
                .value("s3cr3t")
                .type(VariableType.SECRET)
                .build();
        when(variableRepository.findById(1L)).thenReturn(Optional.of(secret));

        VariableDto dto = variableService.get(1L);

        assertEquals("api.password", dto.getName());
        assertEquals(VariableTypeDto.SECRET, dto.getType());
        assertNull(dto.getValue());
    }

    @Test
    void shouldReturnStandardValueUnmaskedWhenReading() {
        Variable standard = Variable.builder()
                .id(1L)
                .name("proxy.url")
                .value("https://proxy.example.com")
                .type(VariableType.STANDARD)
                .build();
        when(variableRepository.findById(1L)).thenReturn(Optional.of(standard));

        VariableDto dto = variableService.get(1L);

        assertEquals("https://proxy.example.com", dto.getValue());
    }

    @Test
    void shouldOverwriteSecretWithNewContent() {
        Variable secret = Variable.builder()
                .id(1L)
                .name("api.password")
                .value("s3cr3t")
                .type(VariableType.SECRET)
                .lastModified(1000L)
                .build();
        when(variableRepository.findById(1L)).thenReturn(Optional.of(secret));

        VariableDto updated = variableService.update(1L, VariableDto.builder()
                .id(1L)
                .name("api.password")
                .value("new-s3cr3t")
                .type(VariableTypeDto.SECRET)
                .scope(VariableScopeDto.GLOBAL)
                .build());

        // the response never unmasks a secret, but the new content must be persisted
        assertNull(updated.getValue());
        assertEquals("new-s3cr3t", secret.getValue());
        // overwriting the value refreshes the last modification timestamp
        assertTrue(secret.getLastModified() > 1000L);
        verify(variableRepository).save(secret);
    }

    @Test
    void shouldKeepStoredValueWhenSecretUpdateOmitsValue() {
        Variable secret = Variable.builder()
                .id(1L)
                .name("api.password")
                .value("s3cr3t")
                .type(VariableType.SECRET)
                .lastModified(1000L)
                .build();
        when(variableRepository.findById(1L)).thenReturn(Optional.of(secret));

        VariableDto updated = variableService.update(1L, VariableDto.builder()
                .id(1L)
                .name("api.password")
                .type(VariableTypeDto.SECRET)
                .scope(VariableScopeDto.GLOBAL)
                .build());

        // the stored value is kept and still never unmasked in the response
        assertNull(updated.getValue());
        assertEquals("s3cr3t", secret.getValue());
        // a metadata-only update does not refresh the last modification timestamp
        assertEquals(1000L, secret.getLastModified());
    }

    @Test
    void shouldNotRefreshLastModifiedWhenValueUnchanged() {
        Variable standard = Variable.builder()
                .id(1L)
                .name("proxy.url")
                .value("https://proxy.example.com")
                .type(VariableType.STANDARD)
                .lastModified(1000L)
                .build();
        when(variableRepository.findById(1L)).thenReturn(Optional.of(standard));

        variableService.update(1L, VariableDto.builder()
                .id(1L)
                .name("proxy.url")
                .value("https://proxy.example.com")
                .type(VariableTypeDto.STANDARD)
                .scope(VariableScopeDto.GLOBAL)
                .build());

        assertEquals(1000L, standard.getLastModified());
    }

    @Test
    void shouldRejectConversionOfSecretToStandardType() {
        Variable secret = Variable.builder()
                .id(1L)
                .name("api.password")
                .value("s3cr3t")
                .type(VariableType.SECRET)
                .build();
        when(variableRepository.findById(1L)).thenReturn(Optional.of(secret));

        assertThrows(ProcessingException.class, () -> variableService.update(1L, VariableDto.builder()
                .id(1L)
                .name("api.password")
                .value("whatever")
                .type(VariableTypeDto.STANDARD)
                .scope(VariableScopeDto.GLOBAL)
                .build()));
    }

    @Test
    void shouldRejectDomainScopeWithoutDomain() {
        assertThrows(ProcessingException.class, () -> variableService.create(VariableDto.builder()
                .name("proxy.url")
                .value("value")
                .type(VariableTypeDto.STANDARD)
                .scope(VariableScopeDto.DOMAIN)
                .build()));
    }

    @Test
    void shouldRejectUnknownDomain() {
        when(domainRepository.existsById(99L)).thenReturn(false);

        assertThrows(MissingElementException.class, () -> variableService.create(VariableDto.builder()
                .name("proxy.url")
                .value("value")
                .type(VariableTypeDto.STANDARD)
                .scope(VariableScopeDto.DOMAIN)
                .domainId(99L)
                .build()));
    }

    @Test
    void shouldThrowWhenVariableNotFound() {
        when(variableRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(MissingElementException.class, () -> variableService.get(404L));
        assertThrows(MissingElementException.class, () -> variableService.delete(404L));
    }

    @Test
    void shouldCheckVariableExistenceByName() {
        when(variableRepository.existsByName("proxy.url")).thenReturn(true);
        when(variableRepository.existsByName("missing")).thenReturn(false);

        assertTrue(variableService.exists("proxy.url"));
        assertFalse(variableService.exists("missing"));
    }

    @Test
    void shouldReturnRawValueForInternalUse() {
        Variable secret = Variable.builder()
                .id(1L)
                .name("api.password")
                .value("s3cr3t")
                .type(VariableType.SECRET)
                .build();
        when(variableRepository.findByNameAndDomainIsNull("api.password")).thenReturn(Optional.of(secret));

        // internal consumers (e.g. application deployment) need the real value, even for secrets
        assertEquals("s3cr3t", variableService.getRawValue("api.password"));
    }

    @Test
    void shouldThrowWhenRawValueOfMissingVariableRequested() {
        when(variableRepository.findByNameAndDomainIsNull("missing")).thenReturn(Optional.empty());

        assertThrows(MissingElementException.class, () -> variableService.getRawValue("missing"));
    }

    @Test
    void shouldResolveDomainVariableWithPrecedenceOverInstanceLevel() {
        Variable globalVariable = Variable.builder()
                .id(1L)
                .name("proxy.url")
                .value("https://proxy.example.com")
                .type(VariableType.STANDARD)
                .build();
        Variable domainVariable = Variable.builder()
                .id(2L)
                .name("proxy.url")
                .value("https://domain-proxy.example.com")
                .type(VariableType.STANDARD)
                .domain(new Domain(5L))
                .build();
        when(variableRepository.findByNameAndDomainId("proxy.url", 5L)).thenReturn(Optional.of(domainVariable));
        when(variableRepository.findByNameAndDomainIsNull("proxy.url")).thenReturn(Optional.of(globalVariable));

        Optional<VariableDto> resolved = variableService.resolve("proxy.url", 5L);

        assertEquals("https://domain-proxy.example.com", resolved.orElseThrow().getValue());
    }

    @Test
    void shouldFallBackToInstanceLevelWhenNoDomainVariable() {
        Variable globalVariable = Variable.builder()
                .id(1L)
                .name("proxy.url")
                .value("https://proxy.example.com")
                .type(VariableType.STANDARD)
                .build();
        when(variableRepository.findByNameAndDomainId("proxy.url", 5L)).thenReturn(Optional.empty());
        when(variableRepository.findByNameAndDomainIsNull("proxy.url")).thenReturn(Optional.of(globalVariable));

        Optional<VariableDto> resolved = variableService.resolve("proxy.url", 5L);

        assertEquals("https://proxy.example.com", resolved.orElseThrow().getValue());
    }

    @Test
    void shouldListVariablesByScope() {
        Variable globalVariable = Variable.builder()
                .id(1L)
                .name("proxy.url")
                .value("https://proxy.example.com")
                .type(VariableType.STANDARD)
                .build();
        when(variableRepository.findByDomainIsNull()).thenReturn(List.of(globalVariable));
        when(variableRepository.findByDomainId(5L)).thenReturn(List.of());

        List<VariableDto> instanceLevel = variableService.list(VariableScopeDto.GLOBAL, null);
        List<VariableDto> domainLevel = variableService.list(VariableScopeDto.DOMAIN, 5L);

        assertEquals(1, instanceLevel.size());
        assertEquals(0, domainLevel.size());
    }

    @Test
    void shouldListEffectiveVariablesWithDomainPrecedence() {
        Variable globalVariable = Variable.builder()
                .id(1L)
                .name("proxy.url")
                .value("https://proxy.example.com")
                .type(VariableType.STANDARD)
                .build();
        Variable globalOnly = Variable.builder()
                .id(3L)
                .name("logo.url")
                .value("https://example.com/logo.png")
                .type(VariableType.STANDARD)
                .build();
        Variable domainVariable = Variable.builder()
                .id(2L)
                .name("proxy.url")
                .value("https://domain-proxy.example.com")
                .type(VariableType.STANDARD)
                .domain(new Domain(5L))
                .build();
        when(variableRepository.findByDomainIsNull()).thenReturn(List.of(globalVariable, globalOnly));
        when(variableRepository.findByDomainId(5L)).thenReturn(List.of(domainVariable));

        List<VariableDto> effective = variableService.listEffective(5L);

        assertEquals(2, effective.size());
        assertEquals("https://domain-proxy.example.com",
                effective.stream().filter(v -> v.getName().equals("proxy.url")).findFirst().orElseThrow().getValue());
        assertEquals("https://example.com/logo.png",
                effective.stream().filter(v -> v.getName().equals("logo.url")).findFirst().orElseThrow().getValue());
    }

}
