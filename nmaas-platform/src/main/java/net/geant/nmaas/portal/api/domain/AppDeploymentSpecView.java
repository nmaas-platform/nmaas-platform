package net.geant.nmaas.portal.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api.KubernetesTemplateView;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppDeploymentSpecView {

    private Long id;

    @NotNull
    private List<AppDeploymentEnv> supportedDeploymentEnvironments;

    private KubernetesTemplateView kubernetesTemplate;

    private boolean allowSshAccess;

    private boolean exposesWebUI;

    @NotNull
    private List<AppStorageVolumeView> storageVolumes;

    @NotNull
    private List<AppAccessMethodView> accessMethods;

    private Map<String, String> deployParameters;

    private Map<String, String> globalDeployParameters;

}
