package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.model.ConfirmationEmail;
import net.geant.nmaas.portal.api.model.FailureEmail;

public interface NotificationService {

    void sendEmail(ConfirmationEmail confirmationEmail);

    void sendFailureEmail(FailureEmail failureEmail);

}
