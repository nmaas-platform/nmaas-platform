package net.geant.nmaas.externalservices.inventory.network;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface CloudAttachPoint {

    String getRouterName();

    String getRouterId();

    String getRouterInterfaceName();

}