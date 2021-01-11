package net.geant.nmaas.notifications;

import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.google.common.base.Preconditions.checkArgument;

@Component
@AllArgsConstructor
public class NotificationEventListener {

    private final NotificationManager notificationManager;

    @EventListener
    @Transactional
    public void trigger(NotificationEvent event) {
        checkArgument(event.getMailAttributes() != null, "Mail attributes cannot be null");
        notificationManager.prepareAndSendMail(event.getMailAttributes());
    }
}
