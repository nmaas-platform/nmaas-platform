package net.geant.nmaas.portal.api.domains;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.api.dto.domains.ResourcesLimitTypeDto;
import net.geant.nmaas.portal.service.ResourcesLimitService;
import org.springframework.stereotype.Component;

@Component("resourcesLimitAccessPolicy")
@RequiredArgsConstructor
public class ResourcesLimitAccessPolicy {

    private final ResourcesLimitService resourcesLimitService;

    public boolean isDomainOrGroupResourcesLimit(Long id) {
        ResourcesLimitTypeDto limitType = resourcesLimitService.getResourcesLimit(id).limitType();
        return ResourcesLimitTypeDto.DOMAIN.equals(limitType) || ResourcesLimitTypeDto.DOMAIN_GROUP.equals(limitType);
    }
}
