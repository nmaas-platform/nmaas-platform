package net.geant.nmaas.externalservices.inventory.kubernetes.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.NamespaceConfigOption;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
class KClusterDeploymentView {

    private Long id;

    private NamespaceConfigOption namespaceConfigOption;

    private String defaultNamespace;

    private String defaultStorageClass;

    private Boolean useInClusterGitLabInstance;

    private String smtpServerHostname;

    private Integer smtpServerPort;

    private String smtpServerUsername;

    private String smtpServerPassword;

    private Boolean forceDedicatedWorkers;
}
