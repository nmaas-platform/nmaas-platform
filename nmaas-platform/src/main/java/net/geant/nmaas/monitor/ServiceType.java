package net.geant.nmaas.monitor;

public enum ServiceType {
    GITLAB{
        @Override
        public String getName(){
            return "GITLAB";
        }
    },
    HELM{
        @Override
        public String getName(){return "HELM";}
    };

    public abstract String getName();
}
