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
    },
    DATABASE{
        @Override
        public String getName(){return "DATABASE";}
    },
    JANITOR{
        @Override
        public String getName(){return "JANITOR";}
    };

    public abstract String getName();
}
