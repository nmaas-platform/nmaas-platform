package net.geant.nmaas.portal.api.domain;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeFileTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.ParameterType;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;

import java.util.List;

@Getter
@Setter
public class AppDeploymentSpec {

    private Long id;

    private List<AppDeploymentEnv> supportedDeploymentEnvironments;

    private KubernetesTemplate kubernetesTemplate;

    private DockerComposeFileTemplate dockerComposeFileTemplate;

    private Integer defaultStorageSpace;

    private Map<ParameterType, String> deployParameters;

    private boolean configFileRepositoryRequired;

}
