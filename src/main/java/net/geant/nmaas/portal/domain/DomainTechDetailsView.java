package net.geant.nmaas.portal.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DomainTechDetailsView implements Serializable {

    private Long id;

    private String domainCodename;

    private String externalServiceDomain;

    private String kubernetesNamespace;

    private String kubernetesStorageClass;

    private String kubernetesIngressClass;
}
