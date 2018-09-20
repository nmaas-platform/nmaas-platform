package net.geant.nmaas.orchestration.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AppDeploymentView {

    private String deploymentId;
    private String deploymentName;
    private String domain;
    private String state;
}
