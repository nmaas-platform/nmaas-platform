package net.geant.nmaas.dcn.deployment.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.geant.nmaas.dcn.deployment.DcnDeploymentType;

@RequiredArgsConstructor
@Getter
public class DcnSpec {

    private final String name;
    private final String domain;
    private final DcnDeploymentType dcnDeploymentType;

}