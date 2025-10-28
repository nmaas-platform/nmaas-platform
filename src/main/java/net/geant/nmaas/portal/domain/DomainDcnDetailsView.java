package net.geant.nmaas.portal.domain;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.dcn.deployment.DcnDeploymentType;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DomainDcnDetailsView implements Serializable {

    private Long id;

    private String domainCodename;

    private boolean dcnConfigured;

    private DcnDeploymentType dcnDeploymentType;

    private List<CustomerNetworkView> customerNetworks;
}
