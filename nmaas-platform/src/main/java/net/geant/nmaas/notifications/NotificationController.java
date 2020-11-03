package net.geant.nmaas.notifications;

import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.portal.api.exception.AuthenticationException;
import net.geant.nmaas.utils.captcha.ValidateCaptcha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mail")
public class NotificationController {

    private ApplicationEventPublisher eventPublisher;

    @Autowired
    public NotificationController(ApplicationEventPublisher eventPublisher){
        this.eventPublisher = eventPublisher;
    }

    /**
     * Public endpoint for contact form usage
     * @param mailAttributes
     * @param token
     */
    @PostMapping
    @ValidateCaptcha
    public void sendMail(@RequestBody MailAttributes mailAttributes, @RequestParam String token){
        // TODO verify if captcha token must be verified

        if(mailAttributes.getMailType().equals(MailType.CONTACT_FORM)) {
            eventPublisher.publishEvent(new NotificationEvent(this, mailAttributes));
        } else {
            throw new AuthenticationException("You are not allowed to send this mail");
        }
    }

    /**
     * Authorized endpoint for administrative usage
     * @param mailAttributes
     */
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void sendMailAuthenticated(@RequestBody MailAttributes mailAttributes) {
        eventPublisher.publishEvent(new NotificationEvent(this, mailAttributes));
    }
}
