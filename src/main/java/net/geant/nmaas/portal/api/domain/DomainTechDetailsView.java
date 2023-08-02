package net.geant.nmaas.portal.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DomainTechDetailsView {

    private Long id;

    private String domainCodename;

    private String externalServiceDomain;

    private String kubernetesNamespace;

    private String kubernetesStorageClass;

    private String kubernetesIngressClass;
}
