package net.geant.nmaas.notifications.types.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * DTO for form type presentation
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FormTypeView {
    @NotBlank
    private String key;
    @NotBlank
    private String access;
    @NotBlank
    private String templateName;
}
