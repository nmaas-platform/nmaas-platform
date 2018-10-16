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
    },
    SHIBBOLETH{
        @Override
        public String getName(){return "SHIBBOLETH";}
    };

    public abstract String getName();
}
