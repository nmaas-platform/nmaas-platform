package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.model.EmailConfirmation;
import net.geant.nmaas.portal.api.model.EmailPasswordReset;

public interface NotificationService {

    void sendConfirmationEmail(EmailConfirmation emailConfirmation);

    void sendResetPasswordEmail(EmailPasswordReset emailPasswordReset);
}
