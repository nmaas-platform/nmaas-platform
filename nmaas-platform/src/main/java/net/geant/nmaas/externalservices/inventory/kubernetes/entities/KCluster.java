package net.geant.nmaas.externalservices.inventory.kubernetes.entities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Set of properties describing a Kubernetes cluster deployed in the system
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="k_cluster")
public class KCluster {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    /** Some unique human readable name assigned for the cluster */
    @Column(nullable = false, unique = true)
    private String name;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    private KClusterHelm helm;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    private KClusterApi api;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    private KClusterIngress ingress;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    private KClusterDeployment deployment;

    /**
     * Detailed information on how the cluster is connected to the network. This information is required to feed VPN
     * configuration process done with Ansible.
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private KClusterAttachPoint attachPoint;

    /** All public networks made available for the cluster. Each customer is assigned with a dedicated network. */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<KClusterExtNetwork> externalNetworks = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public KClusterHelm getHelm() {
        return helm;
    }

    public void setHelm(KClusterHelm helm) {
        this.helm = helm;
    }

    public KClusterApi getApi() {
        return api;
    }

    public void setApi(KClusterApi api) {
        this.api = api;
    }

    public KClusterIngress getIngress() {
        return ingress;
    }

    public void setIngress(KClusterIngress ingress) {
        this.ingress = ingress;
    }

    public KClusterDeployment getDeployment() {
        return deployment;
    }

    public void setDeployment(KClusterDeployment deployment) {
        this.deployment = deployment;
    }

    public KClusterAttachPoint getAttachPoint() {
        return attachPoint;
    }

    public void setAttachPoint(KClusterAttachPoint attachPoint) {
        this.attachPoint = attachPoint;
    }

    public List<KClusterExtNetwork> getExternalNetworks() {
        return externalNetworks;
    }

    public void setExternalNetworks(List<KClusterExtNetwork> externalNetworks) {
        this.externalNetworks = externalNetworks;
    }
}
