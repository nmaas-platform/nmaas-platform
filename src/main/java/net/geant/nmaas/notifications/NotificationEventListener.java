package net.geant.nmaas.notifications;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
public class NotificationEventListener {

    private final NotificationManager notificationManager;

    @EventListener
    @Transactional
    public void trigger(NotificationEvent event) {
        Validate.isTrue(event.getMailAttributes() != null, "Mail attributes cannot be null");
        notificationManager.prepareAndSendMail(event.getMailAttributes());
    }
}
