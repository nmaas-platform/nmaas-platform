package net.geant.nmaas.externalservices.inventory.kubernetes.entities;

import javax.persistence.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Set of properties describing a Kubernetes cluster deployed in the system
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="kubernetes_cluster")
public class KubernetesCluster {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name="id")
    private Long id;

    /**
     * Some unique human readable name assigned for the cluster
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Address of the host of which helm commands should be executed
     */
    @Column(nullable = false)
    private InetAddress helmHostAddress;

    /**
     * Username to be used to connect to host with SSH and execute helm commands
     */
    @Column(nullable = false)
    private String helmHostSshUsername;

    /**
     * Directory on the helm host in which all charts are stored
     */
    private String helmHostChartsDirectory;

    /**
     * Address of the host on which Kubernetes REST API is exposed
     */
    @Column(nullable = false)
    private InetAddress restApiHostAddress;

    /**
     * Port on which Kubernetes REST API is exposed
     */
    @Column(nullable = false)
    private int restApiPort;

    /**
     * Detailed information on how the cluster is connected to the network. This information is required to feed VPN
     * configuration process done with Ansible.
     */
    @OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
    private KubernetesClusterAttachPoint attachPoint;

    /**
     * All public networks made available for the cluster. Each customer is assigned with a dedicated network.
     */
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ExternalNetworkSpec> externalNetworks = new ArrayList<>();

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

    public InetAddress getHelmHostAddress() {
        return helmHostAddress;
    }

    public void setHelmHostAddress(InetAddress helmHostAddress) {
        this.helmHostAddress = helmHostAddress;
    }

    public String getHelmHostSshUsername() {
        return helmHostSshUsername;
    }

    public void setHelmHostSshUsername(String helmHostSshUsername) {
        this.helmHostSshUsername = helmHostSshUsername;
    }

    public String getHelmHostChartsDirectory() {
        return helmHostChartsDirectory;
    }

    public void setHelmHostChartsDirectory(String helmHostChartsDirectory) {
        this.helmHostChartsDirectory = helmHostChartsDirectory;
    }

    public InetAddress getRestApiHostAddress() {
        return restApiHostAddress;
    }

    public void setRestApiHostAddress(InetAddress restApiHostAddress) {
        this.restApiHostAddress = restApiHostAddress;
    }

    public int getRestApiPort() {
        return restApiPort;
    }

    public void setRestApiPort(int restApiPort) {
        this.restApiPort = restApiPort;
    }

    public KubernetesClusterAttachPoint getAttachPoint() {
        return attachPoint;
    }

    public void setAttachPoint(KubernetesClusterAttachPoint attachPoint) {
        this.attachPoint = attachPoint;
    }

    public List<ExternalNetworkSpec> getExternalNetworks() {
        return externalNetworks;
    }

    public void setExternalNetworks(List<ExternalNetworkSpec> externalNetworks) {
        this.externalNetworks = externalNetworks;
    }
}
