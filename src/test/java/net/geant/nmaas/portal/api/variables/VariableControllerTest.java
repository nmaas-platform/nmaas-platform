package net.geant.nmaas.portal.api.variables;

import net.geant.nmaas.api.dto.variables.VariableDto;
import net.geant.nmaas.api.dto.variables.VariableScopeDto;
import net.geant.nmaas.api.dto.variables.VariableTypeDto;
import net.geant.nmaas.portal.service.VariableService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VariableControllerTest {

    private final VariableService variableService = mock(VariableService.class);
    private final VariableController controller = new VariableController(variableService);

    @Test
    void shouldListVariablesByScope() {
        VariableDto dto = testVariableDto(1L);
        when(variableService.list(VariableScopeDto.GLOBAL, null)).thenReturn(List.of(dto));
        when(variableService.list(VariableScopeDto.DOMAIN, 5L)).thenReturn(List.of(dto));

        ResponseEntity<List<VariableDto>> instanceLevel = controller.list(VariableScopeDto.GLOBAL, null);
        ResponseEntity<List<VariableDto>> domainLevel = controller.list(VariableScopeDto.DOMAIN, 5L);

        assertEquals(200, instanceLevel.getStatusCode().value());
        assert instanceLevel.getBody() != null;
        assertEquals(1, instanceLevel.getBody().size());
        assert domainLevel.getBody() != null;
        assertEquals(1, domainLevel.getBody().size());
    }

    @Test
    void shouldListEffectiveVariables() {
        VariableDto dto = testVariableDto(1L);
        when(variableService.listEffective(5L)).thenReturn(List.of(dto));

        ResponseEntity<List<VariableDto>> result = controller.listEffective(5L);

        assertEquals(200, result.getStatusCode().value());
        assert result.getBody() != null;
        assertEquals(1, result.getBody().size());
    }

    @Test
    void shouldGetVariable() {
        VariableDto dto = testVariableDto(1L);
        when(variableService.get(1L)).thenReturn(dto);

        ResponseEntity<VariableDto> result = controller.get(1L);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(dto, result.getBody());
    }

    @Test
    void shouldCreateVariable() {
        VariableDto dto = testVariableDto(1L);
        when(variableService.create(dto)).thenReturn(dto);

        Long result = controller.create(dto);

        assertEquals(1L, result);
        verify(variableService).create(dto);
    }

    @Test
    void shouldUpdateVariableWhenIdsMatch() {
        VariableDto dto = testVariableDto(1L);
        when(variableService.update(1L, dto)).thenReturn(dto);

        ResponseEntity<VariableDto> result = controller.update(1L, dto);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(dto, result.getBody());
    }

    @Test
    void shouldThrowOnUpdateWhenPathAndBodyIdMismatch() {
        VariableDto dto = testVariableDto(1L);

        assertThrows(IllegalArgumentException.class, () -> controller.update(2L, dto));
    }

    @Test
    void shouldDeleteVariable() {
        controller.delete(1L);

        verify(variableService).delete(1L);
    }

    @Test
    void shouldResolveVariable() {
        VariableDto dto = testVariableDto(1L);
        when(variableService.resolve("proxy.url", 5L)).thenReturn(Optional.of(dto));

        ResponseEntity<VariableDto> result = controller.resolve("proxy.url", 5L);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(dto, result.getBody());
    }

    @Test
    void shouldReturnNotFoundWhenVariableDoesNotExist() {
        when(variableService.resolve("missing", 5L)).thenReturn(Optional.empty());

        ResponseEntity<VariableDto> result = controller.resolve("missing", 5L);

        assertEquals(404, result.getStatusCode().value());
    }

    private static VariableDto testVariableDto(Long id) {
        return VariableDto.builder()
                .id(id)
                .name("proxy.url")
                .value("https://proxy.example.com")
                .type(VariableTypeDto.STANDARD)
                .scope(VariableScopeDto.GLOBAL)
                .build();
    }

}
