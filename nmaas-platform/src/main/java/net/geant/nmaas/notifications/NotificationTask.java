package net.geant.nmaas.notifications;

import freemarker.template.TemplateException;
import java.io.IOException;
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
    public void trigger(NotificationEvent event) throws IOException, TemplateException {
        notificationManager.prepareAndSendMail(event.getMailAttributes());
    }
}
