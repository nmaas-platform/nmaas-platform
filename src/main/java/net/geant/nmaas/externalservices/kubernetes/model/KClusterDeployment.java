package net.geant.nmaas.externalservices.kubernetes.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KClusterDeployment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private NamespaceConfigOption namespaceConfigOption;

    private String defaultNamespace;

    private String defaultStorageClass;

    private String smtpServerHostname;

    private Integer smtpServerPort;

    private String smtpServerUsername;

    private String smtpServerPassword;

    private String smtpFromDefaultDomain;

    private Boolean forceDedicatedWorkers;

    @OneToOne(mappedBy = "deployment", cascade = CascadeType.ALL)
    private ClusterManager clusterManager;
}
