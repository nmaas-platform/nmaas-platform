package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities;

import javax.persistence.*;

/**
 * Stores information about network details assigned for particular container.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="docker_container_net_details")
public class DockerContainerNetDetails {

    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column(name="network_id")
    private Long id;

    /**
     * Public port number assigned for accessing services running on container.
     */
    @Column(nullable = false)
    private int publicPort;

    /**
     * Set of IP addresses assigned for container.
     */
    @OneToOne(cascade = CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true, optional=false)
    private DockerNetworkIpamSpec ipAddresses;

    public DockerContainerNetDetails() { }

    public DockerContainerNetDetails(int publicPort, DockerNetworkIpamSpec ipAddresses) {
        this.publicPort = publicPort;
        this.ipAddresses = ipAddresses;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getPublicPort() {
        return publicPort;
    }

    public void setPublicPort(int publicPort) {
        this.publicPort = publicPort;
    }

    public DockerNetworkIpamSpec getIpAddresses() {
        return ipAddresses;
    }

    public void setIpAddresses(DockerNetworkIpamSpec ipAddresses) {
        this.ipAddresses = ipAddresses;
    }
}
