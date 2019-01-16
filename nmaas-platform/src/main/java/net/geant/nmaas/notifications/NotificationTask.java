package net.geant.nmaas.notifications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationTask {

    private NotificationManager notificationManager;

    @Autowired
    public NotificationTask(NotificationManager notificationManager){
        this.notificationManager = notificationManager;
    }

    @EventListener
    public void trigger(NotificationEvent event){
        notificationManager.prepareAndSendMail(event.getMailAttributes());
    }
}
