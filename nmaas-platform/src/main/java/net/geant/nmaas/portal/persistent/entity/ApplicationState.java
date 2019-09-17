package net.geant.nmaas.portal.persistent.entity;

import java.util.Arrays;
import net.geant.nmaas.notifications.templates.MailType;

public enum ApplicationState {
    NEW{
        @Override
        public boolean isChangeAllowed(ApplicationState newState){
            return Arrays.asList(ACTIVE, REJECTED).contains(newState);
        }

        @Override
        public MailType getMailType(){
            return MailType.APP_NEW;
        }
    },
    ACTIVE{
        @Override
        public boolean isChangeAllowed(ApplicationState newState){
            return Arrays.asList(DISABLED, DELETED).contains(newState);
        }

        @Override
        public MailType getMailType(){
            return MailType.APP_ACTIVE;
        }
    },
    REJECTED{
        @Override
        public boolean isChangeAllowed(ApplicationState newState){
            return Arrays.asList(NEW, DELETED).contains(newState);
        }

        @Override
        public MailType getMailType(){
            return MailType.APP_REJECTED;
        }
    },
    DISABLED {
        @Override
        public boolean isChangeAllowed(ApplicationState newState){
            return Arrays.asList(ACTIVE, DELETED).contains(newState);
        }

        @Override
        public MailType getMailType(){
            return MailType.APP_NOT_ACTIVE;
        }
    },
    DELETED{
        @Override
        public boolean isChangeAllowed(ApplicationState newState){
            return false;
        }

        @Override
        public MailType getMailType(){
            return MailType.APP_DELETED;
        }
    };

    public abstract boolean isChangeAllowed(ApplicationState newState);

    public abstract MailType getMailType();
}
