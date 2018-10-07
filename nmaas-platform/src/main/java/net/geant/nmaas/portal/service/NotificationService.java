package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.model.EmailConfirmation;
import org.hibernate.validator.constraints.Email;
import org.springframework.stereotype.Service;

public interface NotificationService {

    void sendEmail(EmailConfirmation emailConfirmation, String token);

}
