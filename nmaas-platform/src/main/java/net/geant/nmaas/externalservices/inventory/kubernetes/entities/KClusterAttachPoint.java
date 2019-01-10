package net.geant.nmaas.externalservices.inventory.kubernetes.entities;

import lombok.Setter;
import net.geant.nmaas.externalservices.inventory.network.CloudAttachPoint;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Setter
@Entity
@Table(name="k_cluster_attach_point")
public class KClusterAttachPoint implements CloudAttachPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false)
    private String routerName;

    @Column(nullable = false)
    private String routerId;

    @Column(nullable = false)
    private String routerInterfaceName;

    private int vlanNumber;

    private String subnet;

    private String gateway;

    public Long getId() {
        return id;
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

    public int getVlanNumber() {
        return vlanNumber;
    }

    public String getSubnet() {
        return subnet;
    }

    public String getGateway() {
        return gateway;
    }
}
