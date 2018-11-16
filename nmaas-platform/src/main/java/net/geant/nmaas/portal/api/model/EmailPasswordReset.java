package net.geant.nmaas.portal.api.model;

import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
public class EmailPasswordReset extends Email{

    @NotNull
    private String accessURL;

    @Builder
    public EmailPasswordReset(String toEmail, String subject, String templateName, String firstName, String userName, String accessURL){
        super(toEmail, subject, templateName, firstName, userName);
        this.accessURL = accessURL;
    }
}
