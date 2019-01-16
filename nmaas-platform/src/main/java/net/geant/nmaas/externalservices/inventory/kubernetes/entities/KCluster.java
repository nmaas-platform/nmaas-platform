package net.geant.nmaas.externalservices.inventory.kubernetes.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Set of properties describing a Kubernetes cluster deployed in the system
 */
@Entity
@Table(name = "k_cluster")
@Getter
@Setter
public class KCluster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

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
        ingress.getCertificateConfigOption().validate(ingress);
        deployment.getNamespaceConfigOption().validate(deployment);
        checkArgument(deployment.getSmtpServerHostname() != null && !deployment.getSmtpServerHostname().isEmpty(), "SMTP server hostname can't be empty");
        checkArgument(deployment.getSmtpServerPort() != null && deployment.getSmtpServerPort() > 0, "SMTP server port must be greater than 0");
    }

}
