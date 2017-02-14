package net.geant.nmaas.nmservice.deployment.nmservice;

import net.geant.nmaas.orchestration.AppUiAccessDetails;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class NmServiceInfo {

    /**
     * Unique name of the service provided by the caller
     */
    private String name;

    /**
     * State in which service should be at this point
     */
    private NmServiceDeploymentState state;

    /**
     * Specification of the service
     */
    private NmServiceSpec spec;

    /**
     * Identifier of the service assigned by orchestrator
     */
    private String deploymentId;

    /**
     * Target deployment host (e.g. Docker Host or Docker Swarm manager) on which this service will be or was deployed.
     */
    private NmServiceDeploymentHost host;

    /**
     * Network details for deployed services obtained from remote OSS system.
     */
    private NmServiceDeploymentNetworkDetails network;

    public NmServiceInfo(String name, NmServiceDeploymentState state, NmServiceSpec spec) {
        this.name = name;
        this.state = state;
        this.spec = spec;
    }

    public void updateState(NmServiceDeploymentState state) {
        this.state = state;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getName() {
        return name;
    }

    public NmServiceSpec getSpec() {
        return spec;
    }

    public NmServiceDeploymentHost getHost() {
        return host;
    }

    public void setHost(NmServiceDeploymentHost host) {
        this.host = host;
    }

    public NmServiceDeploymentNetworkDetails getNetwork() {
        return network;
    }

    public void setNetwork(NmServiceDeploymentNetworkDetails network) {
        this.network = network;
    }

    public NmServiceDeploymentState getState() {
        return state;
    }
}
