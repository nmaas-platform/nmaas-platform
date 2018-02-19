package net.geant.nmaas.externalservices.inventory.kubernetes.entities;

import net.geant.nmaas.orchestration.entities.Identifier;

import javax.persistence.*;
import java.net.InetAddress;
import java.util.Date;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="kubernetes_cluster_external_network_spec")
public class ExternalNetworkSpec {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    /**
     * Concrete Ip address to be configured on the Ingress Controller
     */
    @Column(nullable = false)
    private InetAddress externalIp;

    /**
     * Network to be routed in the customer VPN
     */
    @Column(nullable = false)
    private InetAddress externalNetwork;

    /**
     * Length of the network mask
     */
    private int externalNetworkMaskLength;

    /**
     * Indicates if network is already assigned to any customer
     */
    private boolean assigned = false;

    /**
     * Date of the assignment
     */
    private Date assignedSince;

    /**
     * Identifier of the customer
     */
    private Identifier assignedTo;

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

    public Identifier getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Identifier assignedTo) {
        this.assignedTo = assignedTo;
    }
}
