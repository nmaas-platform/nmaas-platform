package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.domain.ResourcesLimitDto;

import java.util.List;
import java.util.Optional;

public interface ResourcesLimitService {

    ResourcesLimitDto create(ResourcesLimitDto dto);

    void update(ResourcesLimitDto dto);

    void delete(Long id);

    ResourcesLimitDto getResourcesLimit(Long id);

    List<ResourcesLimitDto> getAllResourcesLimits();

}
