package net.geant.nmaas.notifications.types.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for form type presentation
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FormTypeDto {
    @NotBlank
    private String key;
    @NotBlank
    private String access;
    @NotBlank
    private String templateName;
}
