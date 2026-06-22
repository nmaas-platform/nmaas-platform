package net.geant.nmaas.orchestration.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AppDeploymentDto {

    private String deploymentId;
    private String deploymentName;
    private String domain;
    private String state;
    private String owner;
    private String appName;

}
