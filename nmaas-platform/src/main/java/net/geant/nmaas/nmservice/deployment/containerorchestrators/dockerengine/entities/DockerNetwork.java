package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities;

import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.orchestration.entities.Identifier;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="docker_network")
public class DockerNetwork {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    /**
     * Docker Host on which this network was created
     */
    @ManyToOne(fetch=FetchType.EAGER, optional=false)
    private DockerHost dockerHost;

    /**
     * Identifier of the client requesting application deployment
     */
    @Column(nullable=false)
    private Identifier clientId;

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
     * List of containers attached to this network
     */
    @OneToMany(fetch=FetchType.EAGER)
    private List<DockerContainer> dockerContainers = new ArrayList<>();

    /**
     * Identifier assigned by Docker Engine during network deployment
     */
    private String deploymentId;

    /**
     * Human readable name of the network assigned during deployment
     */
    private String deploymentName;

    public DockerNetwork() { }

    public DockerNetwork(Identifier clientId, DockerHost dockerHost) {
        this.clientId = clientId;
        this.dockerHost = dockerHost;
    }

    public DockerNetwork(Identifier clientId, DockerHost dockerHost, int vlanNumber, String subnet, String gateway) {
        this.clientId = clientId;
        this.dockerHost = dockerHost;
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

    public DockerHost getDockerHost() {
        return dockerHost;
    }

    public void setDockerHost(DockerHost dockerHost) {
        this.dockerHost = dockerHost;
    }

    public Identifier getClientId() {
        return clientId;
    }

    public void setClientId(Identifier clientId) {
        this.clientId = clientId;
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

    public List<DockerContainer> getDockerContainers() {
        return dockerContainers;
    }

    public void setDockerContainers(List<DockerContainer> dockerContainers) {
        this.dockerContainers = dockerContainers;
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
