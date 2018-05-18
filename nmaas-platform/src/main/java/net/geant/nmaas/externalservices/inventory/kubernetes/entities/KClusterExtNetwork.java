package net.geant.nmaas.externalservices.inventory.kubernetes.entities;

import javax.persistence.*;
import java.net.InetAddress;
import java.util.Date;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="k_cluster_ext_network")
public class KClusterExtNetwork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Concrete IP address to be configured on the Ingress Controller
     */
    @Column(nullable = false)
    private InetAddress externalIp;

    /**
     * Network to be routed in the DCN
     */
    @Column(nullable = false)
    private InetAddress externalNetwork;

    /**
     * Length of the network mask
     */
    private int externalNetworkMaskLength;

    /**
     * Indicates if network is already assigned to any domain
     */
    private boolean assigned = false;

    /**
     * Date of the assignment
     */
    private Date assignedSince;

    /**
     * Name of the domain
     */
    private String assignedTo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InetAddress getExternalIp() {
        return externalIp;
    }

    public void setExternalIp(InetAddress externalIp) {
        this.externalIp = externalIp;
    }

    public InetAddress getExternalNetwork() {
        return externalNetwork;
    }

    public void setExternalNetwork(InetAddress externalNetwork) {
        this.externalNetwork = externalNetwork;
    }

    public int getExternalNetworkMaskLength() {
        return externalNetworkMaskLength;
    }

    public void setExternalNetworkMaskLength(int externalNetworkMaskLength) {
        this.externalNetworkMaskLength = externalNetworkMaskLength;
    }

    public boolean isAssigned() {
        return assigned;
    }

    public void setAssigned(boolean assigned) {
        this.assigned = assigned;
    }

    public Date getAssignedSince() {
        return assignedSince;
    }

    public void setAssignedSince(Date assignedSince) {
        this.assignedSince = assignedSince;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
}
