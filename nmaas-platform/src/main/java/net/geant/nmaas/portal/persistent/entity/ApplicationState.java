package net.geant.nmaas.portal.persistent.entity;

import java.util.Arrays;

public enum ApplicationState {
    NEW{
        @Override
        public boolean isChangeAllowed(ApplicationState newState){
            return Arrays.asList(ACTIVE, REJECTED).contains(newState);
        }
    },
    ACTIVE{
        @Override
        public boolean isChangeAllowed(ApplicationState newState){
            return Arrays.asList(NOT_ACTIVE, DELETED).contains(newState);
        }
    },
    REJECTED{
        @Override
        public boolean isChangeAllowed(ApplicationState newState){
            return Arrays.asList(NEW, DELETED).contains(newState);
        }
    },
    NOT_ACTIVE{
        @Override
        public boolean isChangeAllowed(ApplicationState newState){
            return Arrays.asList(ACTIVE, DELETED).contains(newState);
        }
    },
    DELETED{
        @Override
        public boolean isChangeAllowed(ApplicationState newState){
            return false;
        }
    };

    public abstract boolean isChangeAllowed(ApplicationState newState);
}
