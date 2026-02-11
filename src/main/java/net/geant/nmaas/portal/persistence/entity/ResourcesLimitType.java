package net.geant.nmaas.portal.persistence.entity;

import net.geant.nmaas.api.dto.ResourcesLimitTypeDto;

public enum ResourcesLimitType {
    GLOBAL,
    DOMAIN,
    DOMAIN_GROUP;

    public static ResourcesLimitType from(ResourcesLimitTypeDto dto) {
        return ResourcesLimitType.valueOf(dto.name());
    }

}
