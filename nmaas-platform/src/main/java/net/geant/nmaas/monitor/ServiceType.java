package net.geant.nmaas.monitor;

public enum ServiceType {
    GITLAB{
        @Override
        public String getName(){
            return "GITLAB";
        }
    };

    public abstract String getName();
}
