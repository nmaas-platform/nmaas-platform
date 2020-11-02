package net.geant.nmaas.notifications.types.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

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

    @NotEmpty
    private List<@Email String> emails;
}
