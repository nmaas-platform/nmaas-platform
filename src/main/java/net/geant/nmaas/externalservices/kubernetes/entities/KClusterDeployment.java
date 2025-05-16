package net.geant.nmaas.externalservices.kubernetes.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "k_cluster_deployment")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KClusterDeployment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private NamespaceConfigOption namespaceConfigOption;

    private String defaultNamespace;

    private String defaultStorageClass;

    private String smtpServerHostname;

    private Integer smtpServerPort;

    private String smtpServerUsername;

    private String smtpServerPassword;

    private String smtpFromDefaultDomain;

    private Boolean forceDedicatedWorkers;

}