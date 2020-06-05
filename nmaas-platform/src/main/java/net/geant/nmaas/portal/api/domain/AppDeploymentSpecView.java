package net.geant.nmaas.portal.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api.KubernetesTemplateView;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppDeploymentSpecView {

    private List<AppDeploymentEnv> supportedDeploymentEnvironments;

    private KubernetesTemplateView kubernetesTemplate;

    private boolean exposesWebUI;

    private List<AppStorageVolumeView> storageVolumes;

    private List<AppAccessMethodView> accessMethods;

    private Map<String, String> deployParameters;

    private Map<String, String> globalDeployParameters;

}
