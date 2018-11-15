package net.geant.nmaas.portal.api.model;

import javax.validation.constraints.NotNull;
import lombok.Builder;

public class EmailPasswordReset extends Email{

    @NotNull
    private String url;

    @Builder
    public EmailPasswordReset(String toEmail, String subject, String templateName, String firstName, String url){
        super(toEmail, subject, templateName, firstName);
        this.url = url;
    }
}
