package net.geant.nmaas.dcn.deployment.api.model;

import net.geant.nmaas.dcn.deployment.entities.DcnInfo;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DcnView {

    private String domain;
    private String state;
    private int vlanNumber;
    private String subnet;

    public DcnView() {}

    public DcnView(DcnInfo dcnInfo) {
        this.domain = dcnInfo.getDomain();
        this.state = dcnInfo.getState().name();
        this.vlanNumber = dcnInfo.getCloudEndpointDetails().getVlanNumber();
        this.subnet = dcnInfo.getCloudEndpointDetails().getSubnet();
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getVlanNumber() {
        return vlanNumber;
    }

    public void setVlanNumber(int vlanNumber) {
        this.vlanNumber = vlanNumber;
    }

    public String getSubnet() {
        return subnet;
    }

    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }
}
