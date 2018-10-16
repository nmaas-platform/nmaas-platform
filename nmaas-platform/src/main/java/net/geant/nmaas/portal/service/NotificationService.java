package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.model.EmailConfirmation;

public interface NotificationService {

    void sendEmail(EmailConfirmation emailConfirmation);

}
