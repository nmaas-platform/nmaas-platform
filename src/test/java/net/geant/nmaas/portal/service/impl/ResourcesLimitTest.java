package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.api.dto.domains.ResourcesLimitDto;
import net.geant.nmaas.api.dto.domains.ResourcesLimitTypeDto;
import net.geant.nmaas.api.dto.domains.ResourcesLimitUpdateDto;
import net.geant.nmaas.api.dto.domains.DomainBaseDto;
import net.geant.nmaas.portal.domain.converters.ResourceLimitConverter;
import net.geant.nmaas.portal.domain.converters.ResourceLimitInverseConverter;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.ResourcesLimit;
import net.geant.nmaas.portal.persistence.entity.ResourcesLimitType;
import net.geant.nmaas.portal.persistence.repositories.ResourcesLimitRepository;
import net.geant.nmaas.portal.service.DomainGroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourcesLimitTest {

    private final ResourcesLimitRepository resourcesLimitRepository = mock(ResourcesLimitRepository.class);

    private final DomainBaseDto domainView = new DomainBaseDto();
    private final ModelMapper mapper = new ModelMapper();
    private final DomainGroupService groupService = mock(DomainGroupService.class);

    private ResourcesLimitDto resourcesLimitDto;
    private ResourcesLimit resourcesLimit;

    private ResourcesLimitServiceImpl resourcesLimitService;

    @BeforeEach
    void setUp() {
        mapper.addConverter(new ResourceLimitConverter());
        mapper.addConverter(new ResourceLimitInverseConverter());
        resourcesLimitService = new ResourcesLimitServiceImpl(resourcesLimitRepository, mapper,groupService);
        domainView.setId(1L);
        resourcesLimitDto = new ResourcesLimitDto(1L, 500, 100, 10, 50,
                ResourcesLimitTypeDto.DOMAIN, null, domainView);
        resourcesLimit = new ResourcesLimit(1L, 500, 100, 10, 50, new Domain(1L));
        when(resourcesLimitRepository.save(isA(ResourcesLimit.class))).thenReturn(resourcesLimit);
        resourcesLimitService.create(resourcesLimitDto);
    }

    @Test
    void crudResourcesLimit() {
        DomainBaseDto domainView2 = new DomainBaseDto();
        domainView2.setId(2L);
        ResourcesLimitDto resourcesLimitDto2 = new ResourcesLimitDto(2L, 500, 100, 10, 50,
                ResourcesLimitTypeDto.DOMAIN, null, domainView2);
        ResourcesLimit resourcesLimit2 = new ResourcesLimit(2L, 500, 100, 10, 50, new Domain(2L));
        resourcesLimit2.setLimitType(ResourcesLimitType.DOMAIN);
        when(resourcesLimitRepository.save(isA(ResourcesLimit.class))).thenReturn(resourcesLimit2);
        ResourcesLimitDto created = resourcesLimitService.create(resourcesLimitDto2);

        assertNotNull(created);
        assertEquals(resourcesLimitDto2.id(), created.id());
        assertEquals(100, created.cpu());
        assertEquals(resourcesLimitDto2.containersNo(), created.containersNo());
        assertEquals(resourcesLimitDto2.limitType(), created.limitType());

        ResourcesLimitUpdateDto updateDto = new ResourcesLimitUpdateDto(created.id(), created.memory(), 1000,
                created.instancesNo(), created.containersNo());
        when(resourcesLimitRepository.findById(2L)).thenReturn(Optional.of(resourcesLimit2));
        resourcesLimitService.update(updateDto);
        resourcesLimit2.setCpu(1000);
        when(resourcesLimitRepository.findById(2L)).thenReturn(Optional.of(resourcesLimit2));
        created = resourcesLimitService.getResourcesLimit(2L);
        assertEquals(1000, created.cpu());
        assertEquals(resourcesLimitDto2.containersNo(), created.containersNo());
        assertEquals(resourcesLimitDto2.limitType(), created.limitType());

        doNothing().when(resourcesLimitRepository).deleteById(2L);
        resourcesLimitService.delete(2L);
    }

    @Test
    void shouldGetAllResourcesLimits() {
        when(resourcesLimitRepository.findAll()).thenReturn(Collections.singletonList(resourcesLimit));

        List<ResourcesLimitDto> resourcesLimits = resourcesLimitService.getAllResourcesLimits();

        assertNotNull(resourcesLimits);
        assertEquals(1, resourcesLimits.size());
        ResourcesLimitDto created = resourcesLimits.getFirst();
        assertEquals(resourcesLimitDto.id(), created.id());
        assertEquals(resourcesLimitDto.cpu(), created.cpu());
        assertEquals(resourcesLimitDto.containersNo(), created.containersNo());
        assertEquals(resourcesLimitDto.limitType(), created.limitType());
    }

    @Test
    void shouldThrowExceptionWhenResourcesLimitNotFound() {
        when(resourcesLimitRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> {
            resourcesLimitService.getResourcesLimit(999L);
        });
    }

}
