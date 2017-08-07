package net.geant.nmaas.externalservices.inventory.network;

import javax.persistence.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="docker_host_attach_point")
public class DockerHostAttachPoint implements CloudAttachPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="id")
    private Long id;

    @Column(nullable=false, unique=true)
    private String dockerHostName;

    @Column(nullable=false)
    private String routerName;

    @Column(nullable=false)
    private String routerId;

    @Column(nullable=false)
    private String routerInterfaceName;

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

    @Override
    public String getRouterName() {
        return routerName;
    }

    public void setRouterName(String routerName) {
        this.routerName = routerName;
    }

    @Override
    public String getRouterId() {
        return routerId;
    }

    public void setRouterId(String routerId) {
        this.routerId = routerId;
    }

    @Override
    public String getRouterInterfaceName() {
        return routerInterfaceName;
    }

    public void setRouterInterfaceName(String routerInterfaceName) {
        this.routerInterfaceName = routerInterfaceName;
    }

}
