package net.geant.nmaas.externalservices.inventory.network;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
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
