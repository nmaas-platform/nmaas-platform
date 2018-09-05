package net.geant.nmaas.externalservices.monitor;

public enum ServiceType {
    GITLAB{
        @Override
        public String getName(){
            return "GitLab";
        }
    };

    public abstract String getName();
}
