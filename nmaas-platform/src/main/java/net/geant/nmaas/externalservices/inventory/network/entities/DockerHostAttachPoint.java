package net.geant.nmaas.externalservices.inventory.network.entities;

import lombok.Setter;
import net.geant.nmaas.externalservices.inventory.network.CloudAttachPoint;

import javax.persistence.*;

@Setter
@Entity
@Table(name="docker_host_attach_point")
public class DockerHostAttachPoint implements CloudAttachPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    public String getDockerHostName() {
        return dockerHostName;
    }

    @Override
    public String getRouterName() {
        return routerName;
    }

    @Override
    public String getRouterId() {
        return routerId;
    }

    @Override
    public String getRouterInterfaceName() {
        return routerInterfaceName;
    }

}
