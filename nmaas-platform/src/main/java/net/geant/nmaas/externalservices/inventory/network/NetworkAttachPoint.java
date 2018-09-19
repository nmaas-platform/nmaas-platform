package net.geant.nmaas.externalservices.inventory.network;

public interface NetworkAttachPoint {

    String getRouterName();

    String getRouterId();

    String getAsNumber();

    String getRouterInterfaceName();

    String getRouterInterfaceUnit();

    String getRouterInterfaceVlan();

    String getBgpLocalIp();

    String getBgpNeighborIp();

}
