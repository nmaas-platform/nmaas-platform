package net.geant.nmaas.portal.persistent.entity;

import javax.persistence.Embeddable;

@Embeddable
public class DomainTechDetails {

    private String kubernetesNamespace;

    private boolean dcnConfigured;

    private String persistentClass;

    public DomainTechDetails(String kubernetesNamespace, boolean dcnConfigured, String persistentClass){
        this.kubernetesNamespace = kubernetesNamespace;
        this.dcnConfigured = dcnConfigured;
        this.persistentClass = persistentClass;
    }

    public DomainTechDetails(){}

    public String getKubernetesNamespace() {
        return kubernetesNamespace;
    }

    public void setKubernetesNamespace(String kubernetesNamespace) {
        this.kubernetesNamespace = kubernetesNamespace;
    }

    public boolean isDcnConfigured() {
        return dcnConfigured;
    }

    public void setDcnConfigured(boolean dcnConfigured) {
        this.dcnConfigured = dcnConfigured;
    }

    public String getPersistentClass() {
        return persistentClass;
    }

    public void setPersistentClass(String persistentClass) {
        this.persistentClass = persistentClass;
    }
}
