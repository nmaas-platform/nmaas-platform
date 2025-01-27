package net.geant.nmaas.notifications.types.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * DTO for form type creation
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FormTypeRequest {

    @NotBlank
    private String key;
    @NotBlank
    private String access;
    @NotBlank
    private String templateName;

    @NotEmpty
    private List<@Email String> emails;

    @NotBlank
    private String subject;
}
