package net.geant.nmaas.externalservices.inventory.kubernetes.entities;

import javax.persistence.*;
import java.net.InetAddress;

/**
 * Set of properties describing a Kubernetes cluster REST API details
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="k_cluster_api")
public class KClusterApi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Address of the host on which Kubernetes REST API is exposed */
    @Column(nullable = false)
    private InetAddress restApiHostAddress;

    /** Port on which Kubernetes REST API is exposed */
    @Column(nullable = false)
    private int restApiPort;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
