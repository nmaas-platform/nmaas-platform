package net.geant.nmaas.portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

public record BundleRequestDto(
        @NotBlank String name,
        @NotBlank String codeName,
        List<BundleDescriptionDto> descriptions,
        @NotNull @Size(min = 2, message = "Bundle must contain at least 2 applications")
        Set<Long> appIds
) {
}

