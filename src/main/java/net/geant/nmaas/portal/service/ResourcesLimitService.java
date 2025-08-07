package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.domain.ResourcesLimitDto;
import net.geant.nmaas.portal.api.domain.ResourcesLimitUpdateDto;

import java.util.List;

public interface ResourcesLimitService {

    ResourcesLimitDto create(ResourcesLimitDto dto);

    void update(ResourcesLimitUpdateDto dto);

    void delete(Long id);

    ResourcesLimitDto getResourcesLimit(Long id);

    List<ResourcesLimitDto> getAllResourcesLimits();

}
