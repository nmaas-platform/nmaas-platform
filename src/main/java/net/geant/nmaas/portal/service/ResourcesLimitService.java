package net.geant.nmaas.portal.service;

import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.portal.domain.ResourcesLimitDto;
import net.geant.nmaas.portal.domain.ResourcesLimitUpdateDto;
import net.geant.nmaas.portal.domain.ResourcesLimitValidationResult;

import java.util.List;

public interface ResourcesLimitService {

    ResourcesLimitDto create(ResourcesLimitDto dto);

    void update(ResourcesLimitUpdateDto dto);

    void delete(Long id);

    ResourcesLimitDto getResourcesLimit(Long id);

    List<ResourcesLimitDto> getAllResourcesLimits();

    ResourcesLimitValidationResult validateNewDeployment(String domainCodename,
                                                         Identifier applicationId,
                                                         int requestedInstances,
                                                         AppDeploymentSpec deploymentSpec);

   ResourcesLimitDto getGlobalResourcesLimit();

   void setGlobalResourcesLimit(ResourcesLimitDto resourcesLimit);

}
