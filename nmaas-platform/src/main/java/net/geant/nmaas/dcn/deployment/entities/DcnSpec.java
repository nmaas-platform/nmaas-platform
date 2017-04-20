package net.geant.nmaas.dcn.deployment.entities;

import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentNetworkDetails;
import net.geant.nmaas.orchestration.entities.Identifier;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DcnSpec {

    private final String name;

    private final Identifier clientId;

    private NmServiceDeploymentNetworkDetails nmServiceDeploymentNetworkDetails;

    public DcnSpec(String name, Identifier clientId) {
        this.name = name;
        this.clientId = clientId;
    }

    public String getName() {
        return name;
    }

    public Identifier getClientId() {
        return clientId;
    }

    public NmServiceDeploymentNetworkDetails getNmServiceDeploymentNetworkDetails() {
        return nmServiceDeploymentNetworkDetails;
    }

    public void setNmServiceDeploymentNetworkDetails(NmServiceDeploymentNetworkDetails nmServiceDeploymentNetworkDetails) {
        this.nmServiceDeploymentNetworkDetails = nmServiceDeploymentNetworkDetails;
    }
}
