package net.geant.nmaas.externalservices.inventory.kubernetes.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Set of properties describing details of service deployment in Kubernetes cluster
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
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
    @Column(nullable = false)
    private String defaultStorageClass;

    /** Flag indicating if a GitLab instance deployed within the cluster should be used for configuration storage */
    @Column(nullable = false)
    private Boolean useInClusterGitLabInstance;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NamespaceConfigOption getNamespaceConfigOption() {
        return namespaceConfigOption;
    }

    public void setNamespaceConfigOption(NamespaceConfigOption namespaceConfigOption) {
        this.namespaceConfigOption = namespaceConfigOption;
    }

    public String getDefaultNamespace() {
        return defaultNamespace;
    }

    public void setDefaultNamespace(String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
    }

    public String getDefaultStorageClass() {
        return defaultStorageClass;
    }

    public void setDefaultStorageClass(String defaultStorageClass) {
        this.defaultStorageClass = defaultStorageClass;
    }

    public Boolean getUseInClusterGitLabInstance() {
        return useInClusterGitLabInstance;
    }

    public void setUseInClusterGitLabInstance(Boolean useInClusterGitLabInstance) {
        this.useInClusterGitLabInstance = useInClusterGitLabInstance;
    }
}
