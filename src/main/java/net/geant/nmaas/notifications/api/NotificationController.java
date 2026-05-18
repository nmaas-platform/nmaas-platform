package net.geant.nmaas.notifications.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.geant.nmaas.notifications.MailAttributes;
import net.geant.nmaas.notifications.NotificationEvent;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.portal.api.exceptions.AuthenticationException;
import net.geant.nmaas.utils.captcha.ValidateCaptcha;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mail")
@Tag(name = "Notifications", description = "Notification management API")
public class NotificationController {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Public endpoint for contact form usage
     */
    @PostMapping
    @ValidateCaptcha
    public void sendMail(@RequestBody MailAttributes mailAttributes, @RequestParam String token) {
        // TODO verify if captcha token must be verified
        MailType mailType = mailAttributes.getMailType();
        if (mailType.equals(MailType.CONTACT_FORM) || mailType.equals(MailType.ISSUE_REPORT) || mailType.equals(MailType.NEW_DOMAIN_REQUEST) || mailType.equals(MailType.VLAB_REQUEST)) {
            eventPublisher.publishEvent(new NotificationEvent(this, mailAttributes));
        } else {
            throw new AuthenticationException("You are not allowed to send this mail");
        }
    }

    /**
     * Authorized endpoint for administrative usage
     */
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void sendMailAuthenticated(@RequestBody MailAttributes mailAttributes) {
        eventPublisher.publishEvent(new NotificationEvent(this, mailAttributes));
    }
}
