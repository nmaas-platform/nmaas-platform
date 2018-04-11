package net.geant.nmaas.externalservices.inventory.dockerhosts.entities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="docker_host_state")
public class DockerHostState {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String dockerHostName;

    @Column(nullable = false)
    private String dockerHostAddressPoolBase;

    @OneToMany(cascade=CascadeType.ALL)
    private List<NumberAssignment> portAssignments = new ArrayList<>();

    @OneToMany(cascade=CascadeType.ALL)
    private List<NumberAssignment> vlanAssignments = new ArrayList<>();

    @OneToMany(cascade=CascadeType.ALL)
    private List<NumberAssignment> addressAssignments = new ArrayList<>();

    public DockerHostState() {
    }

    public DockerHostState(String dockerHostName, String dockerHostAddressPoolBase) {
        this.dockerHostName = dockerHostName;
        this.dockerHostAddressPoolBase = dockerHostAddressPoolBase;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDockerHostName() {
        return dockerHostName;
    }

    public void setDockerHostName(String dockerHostName) {
        this.dockerHostName = dockerHostName;
    }

    public String getDockerHostAddressPoolBase() {
        return dockerHostAddressPoolBase;
    }

    public void setDockerHostAddressPoolBase(String dockerHostAddressPoolBase) {
        this.dockerHostAddressPoolBase = dockerHostAddressPoolBase;
    }

    public List<NumberAssignment> getPortAssignments() {
        return portAssignments;
    }

    public void setPortAssignments(List<NumberAssignment> portAssignments) {
        this.portAssignments = portAssignments;
    }

    public List<NumberAssignment> getVlanAssignments() {
        return vlanAssignments;
    }

    public void setVlanAssignments(List<NumberAssignment> vlanAssignments) {
        this.vlanAssignments = vlanAssignments;
    }

    public List<NumberAssignment> getAddressAssignments() {
        return addressAssignments;
    }

    public void setAddressAssignments(List<NumberAssignment> addressAssignments) {
        this.addressAssignments = addressAssignments;
    }
}
