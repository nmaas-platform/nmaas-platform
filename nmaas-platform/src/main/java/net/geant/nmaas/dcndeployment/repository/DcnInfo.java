package net.geant.nmaas.dcndeployment.repository;

import net.geant.nmaas.dcndeployment.VpnConfig;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DcnInfo {

    private String name;

    private VpnConfig vpnConfig;

    private DcnState state;

    public DcnInfo(String name) {
        this.name = name;
        this.state = DcnState.INIT;
    }

    public void updateState(DcnState state) {
        this.state = state;
    }

    public DcnState getState() {
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

    public enum DcnState {
        INIT,
        VERIFIED,
        CONFIGURED,
        REMOVED,
        ERROR;
    }

}
