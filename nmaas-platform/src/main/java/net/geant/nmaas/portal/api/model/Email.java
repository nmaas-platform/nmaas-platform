package net.geant.nmaas.portal.api.model;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Email {
    @NotNull
    private String toEmail;

    @NotNull
    private String subject;

    @NotNull
    private String templateName;

    private String firstName;
}
