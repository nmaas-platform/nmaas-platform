package net.geant.nmaas.externalservices.inventory.network;

import javax.persistence.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="basic_customer_network_attach_point")
public class BasicCustomerNetworkAttachPoint implements CustomerNetworkAttachPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="id")
    private Long id;

    @Column(nullable=false, unique=true)
    private Long customerId;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
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
}
