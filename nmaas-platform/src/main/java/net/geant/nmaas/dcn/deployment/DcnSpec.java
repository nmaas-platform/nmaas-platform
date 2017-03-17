package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentNetworkDetails;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DcnSpec {

    private final String name;

    private NmServiceDeploymentNetworkDetails nmServiceDeploymentNetworkDetails;

    public DcnSpec(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public NmServiceDeploymentNetworkDetails getNmServiceDeploymentNetworkDetails() {
        return nmServiceDeploymentNetworkDetails;
    }

    public void setNmServiceDeploymentNetworkDetails(NmServiceDeploymentNetworkDetails nmServiceDeploymentNetworkDetails) {
        this.nmServiceDeploymentNetworkDetails = nmServiceDeploymentNetworkDetails;
    }
}
