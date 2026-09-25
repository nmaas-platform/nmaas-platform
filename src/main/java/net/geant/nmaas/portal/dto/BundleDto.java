package net.geant.nmaas.portal.dto;

import net.geant.nmaas.api.dto.applications.ApplicationBaseDto;

import java.util.List;
import java.util.Set;

public record BundleDto(
        Long id,
        String name,
        String codeName,
        List<BundleDescriptionDto> descriptions,
        Set<ApplicationBaseDto> apps
) {
}
