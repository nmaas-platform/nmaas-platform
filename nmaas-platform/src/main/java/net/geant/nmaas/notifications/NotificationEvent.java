package net.geant.nmaas.notifications;

import org.springframework.context.ApplicationEvent;

public class NotificationEvent extends ApplicationEvent {

    private MailAttributes mailAttributes;

    public NotificationEvent(Object source, MailAttributes mailAttributes){
        super(source);
        this.mailAttributes = mailAttributes;
    }

    MailAttributes getMailAttributes() {
        return mailAttributes;
    }
}
