package net.geant.nmaas.dcn.deployment.api.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.dcn.deployment.entities.DcnInfo;

@NoArgsConstructor
@Getter
@Setter
public class DcnView {

    private String domain;
    private String state;
    private int vlanNumber;
    private String subnet;

    public DcnView(DcnInfo dcnInfo) {
        this.domain = dcnInfo.getDomain();
        this.state = dcnInfo.getState().name();
        this.vlanNumber = dcnInfo.getCloudEndpointDetails().getVlanNumber();
        this.subnet = dcnInfo.getCloudEndpointDetails().getSubnet();
    }

}
