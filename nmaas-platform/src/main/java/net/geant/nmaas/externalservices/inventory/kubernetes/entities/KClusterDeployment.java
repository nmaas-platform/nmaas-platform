package net.geant.nmaas.externalservices.inventory.kubernetes.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Set of properties describing details of service deployment in Kubernetes cluster
 */
@Getter
@Setter
@Entity
@Table(name="k_cluster_deployment")
public class KClusterDeployment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Flag indicating whether to use the default template for all deployments */
    @Column(nullable = false)
    private NamespaceConfigOption namespaceConfigOption;

    /** Kubernetes default namespace for NMaaS deployments */
    private String defaultNamespace;

    /** Kubernetes default storage class to be used by PVCs */
    private String defaultStorageClass;

    /** Flag indicating if a GitLab instance deployed within the cluster should be used for configuration storage */
    @Column(nullable = false)
    private Boolean useInClusterGitLabInstance;

    /** The IP address / hostname of the SMTP server */
    @Column(nullable = false)
    private String smtpServerAddress;

    /** Port on which SMTP server is exposed */
    @Column(nullable = false)
    private Integer smtpServerPort;
}
