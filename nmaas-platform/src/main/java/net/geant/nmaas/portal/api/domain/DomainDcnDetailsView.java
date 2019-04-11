package net.geant.nmaas.portal.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.dcn.deployment.DcnDeploymentType;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DomainDcnDetailsView {

    private Long id;

    private String domainCodename;

    private boolean dcnConfigured;

    private DcnDeploymentType dcnDeploymentType;
}
