package net.geant.nmaas.portal.service;

import net.geant.nmaas.api.dto.domains.ResourcesLimitDto;
import net.geant.nmaas.api.dto.domains.ResourcesLimitUpdateDto;

import java.util.List;

public interface ResourcesLimitService {

    ResourcesLimitDto create(ResourcesLimitDto dto);

    void update(ResourcesLimitUpdateDto dto);

    void delete(Long id);

    ResourcesLimitDto getResourcesLimit(Long id);

    List<ResourcesLimitDto> getAllResourcesLimits();

    ResourcesLimitDto getGlobalResourcesLimit();

    void setGlobalResourcesLimit(ResourcesLimitDto resourcesLimit);

    ResourcesLimitDto getDomainResourceLimit(Long domainId);

    ResourcesLimitDto getGroupResourceLimit(Long groupId);
}
