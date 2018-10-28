package net.geant.nmaas.portal.api.model;

import lombok.*;

import javax.validation.constraints.NotNull;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmailConfirmation extends Email{

    private String lastName;
    private String userName;
    private String appName;
    private String appInstanceName;
    private String domainName;

    @Builder
    public EmailConfirmation(@NotNull String toEmail, @NotNull String subject, @NotNull String templateName, @NotNull String firstName, String lastName, String userName, String appName, String appInstanceName, String domainName) {
        super(toEmail, subject, templateName, firstName);
        this.lastName = lastName;
        this.userName = userName;
        this.appName = appName;
        this.appInstanceName = appInstanceName;
        this.domainName = domainName;
    }
}
