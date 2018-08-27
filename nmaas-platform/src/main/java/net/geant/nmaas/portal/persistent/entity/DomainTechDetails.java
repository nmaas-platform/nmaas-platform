package net.geant.nmaas.portal.persistent.entity;

import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DomainTechDetails {

    private boolean dcnConfigured;

    private String kubernetesNamespace;

    private String kubernetesStorageClass;

}
