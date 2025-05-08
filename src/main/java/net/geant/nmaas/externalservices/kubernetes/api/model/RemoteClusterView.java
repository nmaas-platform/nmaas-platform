package net.geant.nmaas.externalservices.kubernetes.api.model;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.externalservices.kubernetes.entities.KClusterState;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RemoteClusterView {

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

    private KClusterState state;

}