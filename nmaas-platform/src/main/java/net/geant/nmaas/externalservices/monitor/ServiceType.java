package net.geant.nmaas.externalservices.monitor;

public enum ServiceType {
    GITLAB{
        @Override
        public String getName(){
            return "GITLAB";
        }
    };

    public abstract String getName();
}
