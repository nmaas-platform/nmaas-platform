package net.geant.nmaas.externalservices.inventory.network;

import javax.persistence.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
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

    public void setId(Long id) {
        this.id = id;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
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
    public String getAsNumber() {
        return asNumber;
    }

    public void setAsNumber(String asNumber) {
        this.asNumber = asNumber;
    }

    @Override
    public String getRouterInterfaceName() {
        return routerInterfaceName;
    }

    public void setRouterInterfaceName(String routerInterfaceName) {
        this.routerInterfaceName = routerInterfaceName;
    }

    @Override
    public String getRouterInterfaceUnit() {
        return routerInterfaceUnit;
    }

    public void setRouterInterfaceUnit(String routerInterfaceUnit) {
        this.routerInterfaceUnit = routerInterfaceUnit;
    }

    @Override
    public String getRouterInterfaceVlan() {
        return routerInterfaceVlan;
    }

    public void setRouterInterfaceVlan(String routerInterfaceVlan) {
        this.routerInterfaceVlan = routerInterfaceVlan;
    }

    @Override
    public String getBgpLocalIp() {
        return bgpLocalIp;
    }

    public void setBgpLocalIp(String bgpLocalIp) {
        this.bgpLocalIp = bgpLocalIp;
    }

    @Override
    public String getBgpNeighborIp() {
        return bgpNeighborIp;
    }

    public void setBgpNeighborIp(String bgpNeighborIp) {
        this.bgpNeighborIp = bgpNeighborIp;
    }

    public DomainNetworkMonitoredEquipment getMonitoredEquipment() {
        return monitoredEquipment;
    }

    public void setMonitoredEquipment(DomainNetworkMonitoredEquipment monitoredEquipment) {
        this.monitoredEquipment = monitoredEquipment;
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
