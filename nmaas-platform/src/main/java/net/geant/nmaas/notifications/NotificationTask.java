package net.geant.nmaas.notifications;

import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.google.common.base.Preconditions.checkArgument;

@Component
public class NotificationTask {

    private NotificationManager notificationManager;

    @Autowired
    public NotificationTask(NotificationManager notificationManager){
        this.notificationManager = notificationManager;
    }

    @EventListener
    @Loggable(LogLevel.DEBUG)
    @Transactional
    public void trigger(NotificationEvent event) {
        checkArgument(event.getMailAttributes() != null, "Mail attributes cannot be null");
        notificationManager.prepareAndSendMail(event.getMailAttributes());
    }
}
