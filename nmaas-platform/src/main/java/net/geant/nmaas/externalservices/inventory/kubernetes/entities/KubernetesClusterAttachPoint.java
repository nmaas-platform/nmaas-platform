package net.geant.nmaas.externalservices.inventory.kubernetes.entities;

import net.geant.nmaas.externalservices.inventory.network.CloudAttachPoint;

import javax.persistence.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="kubernetes_cluster_attach_point")
public class KubernetesClusterAttachPoint implements CloudAttachPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

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
