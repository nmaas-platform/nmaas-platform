package net.geant.nmaas.dcndeployment.repository;

import net.geant.nmaas.dcndeployment.DcnDeploymentState;
import net.geant.nmaas.dcndeployment.VpnConfig;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DcnInfo {

    private String name;

    private VpnConfig vpnConfig;

    private DcnDeploymentState state;

    public DcnInfo(String name) {
        this.name = name;
        this.state = DcnDeploymentState.INIT;
    }

    public void updateState(DcnDeploymentState state) {
        this.state = state;
    }

    public DcnDeploymentState getState() {
        return state;
    }

    public String getName() {
        return name;
    }

    public VpnConfig getVpnConfig() {
        return vpnConfig;
    }

    public void setVpnConfig(VpnConfig vpnConfig) {
        this.vpnConfig = vpnConfig;
    }

}
