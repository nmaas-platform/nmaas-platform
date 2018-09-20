package net.geant.nmaas.nmservice.deployment.entities;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name="docker_host_network")
public class DockerHostNetwork {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    /**
     * Docker Host on which this network was created
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "host_id")
    private DockerHost host;

    /**
     * Name of the client domain for this deployment
     */
    @Column(nullable=false)
    private String domain;

    /**
     * Assigned VLAN number unique for Docker Host
     */
    private int vlanNumber;

    /**
     * Assigned IP subnet with mask (e.g. 10.10.0.0/24)
     */
    private String subnet;

    /**
     * Assigned IP address of the gateway
     */
    private String gateway;

    /**
     * List of entities for which an IP address was assigned from this Docker Host network subnet.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> assignedAddresses = new ArrayList<>();

    /**
     * Identifier assigned by Docker Engine during network deployment
     */
    private String deploymentId;

    /**
     * Human readable name of the network assigned during deployment
     */
    private String deploymentName;

    public DockerHostNetwork(String domain, DockerHost dockerHost) {
        this.domain = domain;
        this.host = dockerHost;
    }

    public DockerHostNetwork(String domain, DockerHost dockerHost, int vlanNumber, String subnet, String gateway) {
        this.domain = domain;
        this.host = dockerHost;
        this.vlanNumber = vlanNumber;
        this.subnet = subnet;
        this.gateway = gateway;
    }
}
