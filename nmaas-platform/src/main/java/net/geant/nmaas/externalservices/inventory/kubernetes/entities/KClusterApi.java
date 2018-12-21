package net.geant.nmaas.externalservices.inventory.kubernetes.entities;

import javax.persistence.*;
import java.net.InetAddress;
import lombok.Getter;
import lombok.Setter;

/**
 * Set of properties describing a Kubernetes cluster REST API details
 */
@Entity
@Table(name="k_cluster_api")
@Getter
@Setter
public class KClusterApi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Address of the host on which Kubernetes REST API is exposed */
    private InetAddress restApiHostAddress;

    /** Port on which Kubernetes REST API is exposed */
    private Integer restApiPort;

    private boolean useKClusterApi = false;
}
