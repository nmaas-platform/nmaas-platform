package net.geant.nmaas.dcn.deployment.repository;

import net.geant.nmaas.dcn.deployment.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.DcnSpec;
import net.geant.nmaas.dcn.deployment.VpnConfig;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DcnInfo {

    private String name;

    private DcnDeploymentState state;

    private DcnSpec spec;

    private VpnConfig vpnConfig;

    public DcnInfo(String name, DcnDeploymentState state, DcnSpec spec) {
        this.name = name;
        this.state = state;
        this.spec = spec;
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

    public DcnSpec getSpec() {
        return spec;
    }

    public VpnConfig getVpnConfig() {
        return vpnConfig;
    }

    public void setVpnConfig(VpnConfig vpnConfig) {
        this.vpnConfig = vpnConfig;
    }

}
