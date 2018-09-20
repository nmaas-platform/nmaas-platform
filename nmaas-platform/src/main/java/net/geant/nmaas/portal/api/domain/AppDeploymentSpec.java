package net.geant.nmaas.portal.api.domain;

import lombok.Getter;
import lombok.Setter;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;

import java.util.List;

@Getter
@Setter
public class AppDeploymentSpec {

    private Long id;

    private List<AppDeploymentEnv> supportedDeploymentEnvironments;

    private Integer defaultStorageSpace;

    private boolean configFileRepositoryRequired;

}
