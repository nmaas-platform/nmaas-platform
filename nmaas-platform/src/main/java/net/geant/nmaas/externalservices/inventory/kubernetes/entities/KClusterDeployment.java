package net.geant.nmaas.externalservices.inventory.kubernetes.entities;

import javax.persistence.*;

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
    private Boolean useDefaultNamespace;

    /** Kubernetes namespace for NMaaS deployments */
    private String defaultNamespace;

    /** Kubernetes persistence storage class to be used by PVCs */
    @Column(nullable = false)
    private String defaultPersistenceClass;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getUseDefaultNamespace() {
        return useDefaultNamespace;
    }

    public void setUseDefaultNamespace(Boolean useDefaultNamespace) {
        this.useDefaultNamespace = useDefaultNamespace;
    }

    public String getDefaultNamespace() {
        return defaultNamespace;
    }

    public void setDefaultNamespace(String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
    }

    public String getDefaultPersistenceClass() {
        return defaultPersistenceClass;
    }

    public void setDefaultPersistenceClass(String defaultPersistenceClass) {
        this.defaultPersistenceClass = defaultPersistenceClass;
    }
}
