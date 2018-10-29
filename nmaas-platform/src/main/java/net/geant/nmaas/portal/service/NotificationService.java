package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.model.EmailConfirmation;
import net.geant.nmaas.portal.api.model.FailureEmail;

public interface NotificationService {

    void sendEmail(EmailConfirmation emailConfirmation);

    void sendFailureEmail(FailureEmail failureEmail);

}
