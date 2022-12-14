package net.geant.nmaas.dcn.deployment.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.geant.nmaas.dcn.deployment.DcnDeploymentType;

@AllArgsConstructor
@Getter
public class DcnSpec {

    private final String name;

    private final String domain;

    private DcnDeploymentType dcnDeploymentType;
}
