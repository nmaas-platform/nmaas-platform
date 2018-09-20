package net.geant.nmaas.externalservices.inventory.network.entities;

import lombok.Setter;
import net.geant.nmaas.externalservices.inventory.network.NetworkAttachPoint;

import javax.persistence.*;

@Setter
@Entity
@Table(name="domain_network_attach_point")
public class DomainNetworkAttachPoint implements NetworkAttachPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(nullable=false, unique=true)
    private String domain;

    @Column(nullable=false)
    private String routerName;

    @Column(nullable=false)
    private String routerId;

    @Column(nullable=false)
    private String asNumber;

    @Column(nullable=false)
    private String routerInterfaceName;

    @Column(nullable=false)
    private String routerInterfaceUnit;

    @Column(nullable=false)
    private String routerInterfaceVlan;

    @Column(nullable=false)
    private String bgpLocalIp;

    @Column(nullable=false)
    private String bgpNeighborIp;

    // TODO move to a class representing the whole domain
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private DomainNetworkMonitoredEquipment monitoredEquipment;

    public Long getId() {
        return id;
    }

    public String getDomain() {
        return domain;
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
    public String getAsNumber() {
        return asNumber;
    }

    @Override
    public String getRouterInterfaceName() {
        return routerInterfaceName;
    }

    @Override
    public String getRouterInterfaceUnit() {
        return routerInterfaceUnit;
    }

    @Override
    public String getRouterInterfaceVlan() {
        return routerInterfaceVlan;
    }

    @Override
    public String getBgpLocalIp() {
        return bgpLocalIp;
    }

    @Override
    public String getBgpNeighborIp() {
        return bgpNeighborIp;
    }

    public DomainNetworkMonitoredEquipment getMonitoredEquipment() {
        return monitoredEquipment;
    }

    public DomainNetworkAttachPoint update(DomainNetworkAttachPoint domainNetworkAttachPoint) {
        domain = domainNetworkAttachPoint.getDomain();
        routerName = domainNetworkAttachPoint.getRouterName();
        routerId = domainNetworkAttachPoint.getRouterId();
        asNumber = domainNetworkAttachPoint.getAsNumber();
        routerInterfaceName = domainNetworkAttachPoint.getRouterInterfaceName();
        routerInterfaceUnit = domainNetworkAttachPoint.getRouterInterfaceUnit();
        routerInterfaceVlan = domainNetworkAttachPoint.getRouterInterfaceVlan();
        bgpLocalIp = domainNetworkAttachPoint.getBgpLocalIp();
        bgpNeighborIp = domainNetworkAttachPoint.getBgpNeighborIp();
        return this;
    }
}
