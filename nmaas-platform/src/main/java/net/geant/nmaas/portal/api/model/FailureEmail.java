package net.geant.nmaas.portal.api.model;

import lombok.*;

import javax.validation.constraints.NotNull;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FailureEmail extends Email{
    @NotNull
    private String errorMessage;

    @Builder
    public FailureEmail(@NotNull String toEmail, @NotNull String subject, @NotNull String templateName, @NotNull String firstName, @NotNull String errorMessage) {
        super(toEmail, subject, templateName, firstName);
        this.errorMessage = errorMessage;
    }
}
