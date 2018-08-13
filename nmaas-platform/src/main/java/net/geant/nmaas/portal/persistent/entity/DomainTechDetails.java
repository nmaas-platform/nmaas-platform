package net.geant.nmaas.portal.persistent.entity;

import javax.persistence.Embeddable;

@Embeddable
public class DomainTechDetails {

    private boolean dcnConfigured;

    private String kubernetesNamespace;

    private String kubernetesStorageClass;

    public DomainTechDetails(boolean dcnConfigured, String kubernetesNamespace, String kubernetesStorageClass){
        this.dcnConfigured = dcnConfigured;
        this.kubernetesNamespace = kubernetesNamespace;
        this.kubernetesStorageClass = kubernetesStorageClass;
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

    public String getKubernetesStorageClass() {
        return kubernetesStorageClass;
    }

    public void setKubernetesStorageClass(String kubernetesStorageClass) {
        this.kubernetesStorageClass = kubernetesStorageClass;
    }
}
