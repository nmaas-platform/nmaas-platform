package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.domain.DomainBase;
import net.geant.nmaas.portal.domain.ResourcesLimitDto;
import net.geant.nmaas.portal.domain.ResourcesLimitUpdateDto;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.ResourcesLimit;
import net.geant.nmaas.portal.persistence.repositories.ResourcesLimitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
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

    private final DomainBase domainView = new DomainBase();
    private final ModelMapper mapper = new ModelMapper();

    private ResourcesLimitDto resourcesLimitDto;
    private ResourcesLimit resourcesLimit;

    private ResourcesLimitServiceImpl resourcesLimitService;

    @BeforeEach
    void setUp() {
        resourcesLimitService = new ResourcesLimitServiceImpl(resourcesLimitRepository, mapper);
        domainView.setId(1L);
        resourcesLimitDto = new ResourcesLimitDto(1L, 500, 100, 10, 50, domainView);
        resourcesLimit = new ResourcesLimit(1L, 500, 100, 10, 50, new Domain(1L));
        when(resourcesLimitRepository.save(isA(ResourcesLimit.class))).thenReturn(resourcesLimit);
        resourcesLimitService.create(resourcesLimitDto);
    }

    @Test
    void crudResourcesLimit() {
        DomainBase domainView2 = new DomainBase();
        domainView2.setId(2L);
        ResourcesLimitDto resourcesLimitDto2 = new ResourcesLimitDto(2L, 500, 100, 10, 50, domainView2);
        ResourcesLimit resourcesLimit2 = new ResourcesLimit(2L, 500, 100, 10, 50, new Domain(2L));
        when(resourcesLimitRepository.save(isA(ResourcesLimit.class))).thenReturn(resourcesLimit2);
        ResourcesLimitDto created = resourcesLimitService.create(resourcesLimitDto2);

        assertNotNull(created);
        assertEquals(resourcesLimitDto2.getId(), created.getId());
        assertEquals(100, created.getCpu());
        assertEquals(resourcesLimitDto2.getContainersNo(), created.getContainersNo());
        assertEquals(resourcesLimitDto2.getLimitType(), created.getLimitType());

        ResourcesLimitUpdateDto updateDto = mapper.map(created, ResourcesLimitUpdateDto.class);
        updateDto.setCpu(1000);
        when(resourcesLimitRepository.findById(2L)).thenReturn(Optional.of(resourcesLimit2));
        resourcesLimitService.update(updateDto);
        resourcesLimit2.setCpu(1000);
        when(resourcesLimitRepository.findById(2L)).thenReturn(Optional.of(resourcesLimit2));
        created = resourcesLimitService.getResourcesLimit(2L);
        assertEquals(1000, created.getCpu());
        assertEquals(resourcesLimitDto2.getContainersNo(), created.getContainersNo());
        assertEquals(resourcesLimitDto2.getLimitType(), created.getLimitType());

        doNothing().when(resourcesLimitRepository).deleteById(2L);
        resourcesLimitService.delete(2L);
    }

    @Test
    void shouldGetAllResourcesLimits() {
        when(resourcesLimitRepository.findAll()).thenReturn(Arrays.asList(resourcesLimit));

        List<ResourcesLimitDto> resourcesLimits = resourcesLimitService.getAllResourcesLimits();

        assertNotNull(resourcesLimits);
        assertEquals(1, resourcesLimits.size());
        ResourcesLimitDto created = resourcesLimits.get(0);
        assertEquals(resourcesLimitDto.getId(), created.getId());
        assertEquals(resourcesLimitDto.getCpu(), created.getCpu());
        assertEquals(resourcesLimitDto.getContainersNo(), created.getContainersNo());
        assertEquals(resourcesLimitDto.getLimitType(), created.getLimitType());
    }

    @Test
    void shouldThrowExceptionWhenResourcesLimitNotFound() {
        when(resourcesLimitRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            resourcesLimitService.getResourcesLimit(999L);
        });
    }

}
