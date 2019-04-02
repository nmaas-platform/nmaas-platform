package net.geant.nmaas.portal.persistent.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;
import java.io.Serializable;
import net.geant.nmaas.dcn.deployment.DcnDeploymentType;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DomainTechDetails implements Serializable {

    private boolean dcnConfigured;

    private String kubernetesNamespace;

    private String kubernetesStorageClass;

    private DcnDeploymentType dcnDeploymentType;

}
