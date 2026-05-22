package net.geant.nmaas.portal.api.domains;

import net.geant.nmaas.api.dto.Id;
import net.geant.nmaas.api.dto.domains.ResourcesLimitDto;
import net.geant.nmaas.api.dto.domains.ResourcesLimitTypeDto;
import net.geant.nmaas.api.dto.domains.ResourcesLimitUpdateDto;
import net.geant.nmaas.api.dto.domains.DomainBaseDto;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.service.ResourcesLimitService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResourcesLimitControllerTest {

    private final ResourcesLimitService resourcesLimitService = mock(ResourcesLimitService.class);
    private final ResourcesLimitController controller = new ResourcesLimitController(resourcesLimitService);

    @Test
    void shouldSetAndGetGlobalResourcesLimit() {
        ResourcesLimitDto dto = testResourcesLimitDto(10L);
        when(resourcesLimitService.getGlobalResourcesLimit()).thenReturn(dto);

        ResponseEntity<Void> setResult = controller.setGlobalResourcesLimit(dto);
        ResponseEntity<ResourcesLimitDto> getResult = controller.getGlobalResourcesLimit();

        assertEquals(200, setResult.getStatusCode().value());
        assertEquals(dto, getResult.getBody());
        verify(resourcesLimitService).setGlobalResourcesLimit(dto);
        verify(resourcesLimitService).getGlobalResourcesLimit();
    }

    @Test
    void shouldCreateResourcesLimit() {
        ResourcesLimitDto dto = testResourcesLimitDto(11L);
        when(resourcesLimitService.create(dto)).thenReturn(dto);

        ResponseEntity<Id> result = controller.createResourcesLimit(dto);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        verify(resourcesLimitService).create(dto);
    }

    @Test
    void shouldThrowOnUpdateWhenPathAndBodyIdMismatch() {
        ResourcesLimitUpdateDto updateDto = new ResourcesLimitUpdateDto(20L, 500, 100, 10, 50);

        assertThrows(ProcessingException.class, () -> controller.updateResourcesLimit(21L, updateDto));
    }

    @Test
    void shouldUpdateResourcesLimitWhenIdsMatch() {
        ResourcesLimitUpdateDto updateDto = new ResourcesLimitUpdateDto(30L, 500, 100, 10, 50);

        ResponseEntity<Id> result = controller.updateResourcesLimit(30L, updateDto);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        verify(resourcesLimitService).update(updateDto);
    }

    @Test
    void shouldDeleteAndGetResourcesLimits() {
        ResourcesLimitDto dto = testResourcesLimitDto(40L);
        when(resourcesLimitService.getResourcesLimit(40L)).thenReturn(dto);
        when(resourcesLimitService.getAllResourcesLimits()).thenReturn(List.of(dto));
        when(resourcesLimitService.getDomainResourceLimit(4L)).thenReturn(dto);
        when(resourcesLimitService.getGroupResourceLimit(5L)).thenReturn(dto);

        controller.deleteResourcesLimit(40L);
        ResponseEntity<ResourcesLimitDto> one = controller.getResourcesLimit(40L);
        ResponseEntity<List<ResourcesLimitDto>> all = controller.getAllResourcesLimits();
        ResponseEntity<ResourcesLimitDto> domain = controller.getDomainResourceLimit(4L);
        ResponseEntity<ResourcesLimitDto> group = controller.getGroupResourceLimit(5L);

        verify(resourcesLimitService).delete(40L);
        assertEquals(dto, one.getBody());
        assertEquals(1, all.getBody().size());
        assertEquals(dto, domain.getBody());
        assertEquals(dto, group.getBody());
    }

    private static ResourcesLimitDto testResourcesLimitDto(Long id) {
        DomainBaseDto domain = new DomainBaseDto();
        domain.setId(1L);
        return new ResourcesLimitDto(id, 500, 100, 10, 50, ResourcesLimitTypeDto.DOMAIN, null, domain);
    }
}
