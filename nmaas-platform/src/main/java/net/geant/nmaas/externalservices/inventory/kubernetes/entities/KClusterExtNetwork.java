package net.geant.nmaas.externalservices.inventory.kubernetes.entities;

import javax.persistence.*;
import java.net.InetAddress;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
}
