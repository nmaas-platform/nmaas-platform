package net.geant.nmaas.externalservices.kubernetes.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClusterManagerView {

    private Long id;

    private String name;

    private String codename;

    private String description;

    private OffsetDateTime creationDate;

    private OffsetDateTime modificationDate;

    private String pathConfigFile;

    private KClusterView.KClusterIngressView ingress;

    private KClusterView.KClusterDeploymentView deployment;

    private List<String> domainNames;

}
