package net.geant.nmaas.nmservice.deployment.entities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="docker_host_network")
public class DockerHostNetwork {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
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

    public DockerHostNetwork() { }

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DockerHost getHost() {
        return host;
    }

    public void setHost(DockerHost host) {
        this.host = host;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getVlanNumber() {
        return vlanNumber;
    }

    public void setVlanNumber(int vlanNumber) {
        this.vlanNumber = vlanNumber;
    }

    public String getSubnet() {
        return subnet;
    }

    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public List<String> getAssignedAddresses() {
        return assignedAddresses;
    }

    public void setAssignedAddresses(List<String> assignedAddresses) {
        this.assignedAddresses = assignedAddresses;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getDeploymentName() {
        return deploymentName;
    }

    public void setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
    }
}
