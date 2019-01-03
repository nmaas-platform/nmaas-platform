package net.geant.nmaas.externalservices.inventory.network.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DomainNetworkAttachPointView {

    private Long id;

    private String domain;

    private String routerName;

    private String routerId;

    private String asNumber;

    private String routerInterfaceName;

    private String routerInterfaceUnit;

    private String routerInterfaceVlan;

    private String bgpLocalIp;

    private String bgpNeighborIp;

}
