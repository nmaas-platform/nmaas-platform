package net.geant.nmaas.externalservices.inventory.dockerhosts;

import javax.persistence.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="docker_host_state")
public class DockerHostState {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name="id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String dockerHostName;

    @Column(nullable = false)
    private String dockerHostAddressPoolBase;

}
