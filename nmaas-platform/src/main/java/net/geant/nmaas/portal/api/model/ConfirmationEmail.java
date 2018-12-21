package net.geant.nmaas.portal.api.model;

import lombok.*;

import javax.validation.constraints.NotNull;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmationEmail extends Email{

    private String lastName;
    private String userName;
    private String appName;
    private String appInstanceName;
    private String domainName;
    private String accessURL;

    @Builder
    public ConfirmationEmail(@NotNull String toEmail, @NotNull String subject, @NotNull String templateName, @NotNull String firstName, String lastName, String userName, String appName, String appInstanceName, String domainName, String accessURL) {
        super(toEmail, subject, templateName, firstName);
        this.lastName = lastName;
        this.userName = userName;
        this.appName = appName;
        this.appInstanceName = appInstanceName;
        this.domainName = domainName;
        this.accessURL = accessURL;
    }
}