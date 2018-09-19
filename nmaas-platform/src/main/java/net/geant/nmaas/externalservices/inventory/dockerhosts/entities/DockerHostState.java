package net.geant.nmaas.externalservices.inventory.dockerhosts.entities;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
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

    public DockerHostState(String dockerHostName, String dockerHostAddressPoolBase) {
        this.dockerHostName = dockerHostName;
        this.dockerHostAddressPoolBase = dockerHostAddressPoolBase;
    }

}
