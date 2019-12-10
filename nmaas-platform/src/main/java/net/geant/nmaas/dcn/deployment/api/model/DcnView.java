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

    public DcnView(DcnInfo dcnInfo) {
        this.domain = dcnInfo.getDomain();
        this.state = dcnInfo.getState().name();
    }

}
