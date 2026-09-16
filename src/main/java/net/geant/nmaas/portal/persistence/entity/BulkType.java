package net.geant.nmaas.portal.persistence.entity;

public enum BulkType {
    DOMAIN,
    APPLICATION,
    USER;

    public static BulkType from(net.geant.nmaas.api.dto.bulks.BulkTypeDto dto) {
        return BulkType.valueOf(dto.name());
    }

}
