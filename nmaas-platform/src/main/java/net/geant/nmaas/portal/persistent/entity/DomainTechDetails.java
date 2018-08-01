package net.geant.nmaas.portal.persistent.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

@Embeddable
public class DomainTechDetails {

    private String kubernetesNamespace;

    private boolean dcnConfigured;

    public DomainTechDetails(String kubernetesNamespace, boolean dcnConfigured){
        this.kubernetesNamespace = kubernetesNamespace;
        this.dcnConfigured = dcnConfigured;
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
}
