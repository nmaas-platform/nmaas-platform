package net.geant.nmaas.portal.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Email {

    @NotNull
    private String toEmail;

    @NotNull
    private String subject;

    @NotNull
    private String templateName;

    @NotNull
    private String firstName;
}
