package net.geant.nmaas.externalservices.inventory.kubernetes.entities;

import static com.google.common.base.Preconditions.checkArgument;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Set of properties describing a Kubernetes cluster deployed in the system
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="k_cluster")
@Getter
@Setter
public class KCluster {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

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

    public void validate() {
        ingress.getControllerConfigOption().validate(ingress);
        ingress.getResourceConfigOption().validate(ingress);
        deployment.getNamespaceConfigOption().validate(deployment);
        if(api.isUseKClusterApi()){
            checkArgument(api.getRestApiPort() != null, "When using KCluster Api the rest api port can't be empty");
            checkArgument(api.getRestApiHostAddress() != null, "When using KCluster Api the rest api host address can't be empty");
        }
    }

}
