package net.geant.nmaas.portal.api.domain;

import lombok.Getter;
import lombok.Setter;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api.KubernetesTemplateView;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AppDeploymentSpec {

    private List<AppDeploymentEnv> supportedDeploymentEnvironments;

    private KubernetesTemplateView kubernetesTemplate;

    private Integer defaultStorageSpace;

    private boolean exposesWebUI;

    private Map<ParameterType, String> deployParameters;

}
