package net.geant.nmaas.servicedeployment.orchestrators.dockerengine.network;

import net.geant.nmaas.servicedeployment.nmservice.NmServiceDeploymentNetworkDetails;

/**
 * Stores information about network details assigned for particular container deployment.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class ContainerNetworkDetails implements NmServiceDeploymentNetworkDetails {

    private final ContainerNetworkIpamSpec ipAddresses;

    private final int vlanNumber;

    /**
     * Identifier of the network assigned by orchestrator
     */
    private String deploymentId;

    public ContainerNetworkDetails(ContainerNetworkIpamSpec ipAddresses, int vlanNumber) {
        this.ipAddresses = ipAddresses;
        this.vlanNumber = vlanNumber;
    }

    public ContainerNetworkIpamSpec getIpAddresses() {
        return ipAddresses;
    }

    public int getVlanNumber() {
        return vlanNumber;
    }

    @Override
    public void setId(String id) {
        deploymentId = id;
    }

    public String getDeploymentId() {
        return deploymentId;
    }
}
