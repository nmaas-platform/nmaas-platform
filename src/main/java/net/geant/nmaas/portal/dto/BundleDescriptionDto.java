package net.geant.nmaas.portal.dto;

import jakarta.validation.constraints.NotBlank;

public record BundleDescriptionDto(
        Long id,
        @NotBlank
        String language,
        @NotBlank
        String briefDescription,
        @NotBlank
        String fullDescription
) {
}
