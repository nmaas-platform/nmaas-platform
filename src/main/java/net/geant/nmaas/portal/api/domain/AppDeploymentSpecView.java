package net.geant.nmaas.portal.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.model.KubernetesTemplateView;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppDeploymentSpecView {

    private static Integer DEFAULT_CONSUMED_PODS = 1;
    private static Integer DEFAULT_CONSUMED_MEMORY = 128;
    private static Integer DEFAULT_CONSUMED_CPU = 100;

    private Long id;

    @NotNull
    private List<AppDeploymentEnv> supportedDeploymentEnvironments = new ArrayList<>();

    private KubernetesTemplateView kubernetesTemplate = new KubernetesTemplateView();

    private boolean allowSshAccess;

    private boolean allowLogAccess;

    private boolean exposesWebUI;

    @NotNull
    private List<AppStorageVolumeView> storageVolumes = new ArrayList<>();

    @NotNull
    private List<AppAccessMethodView> accessMethods = new ArrayList<>();

    private Map<String, String> deployParameters;

    private Map<String, String> globalDeployParameters;

    private Integer consumedPods;

    private Integer consumedMemory;

    private Integer consumedCpu;

    public Integer getConsumedPods() {
        return consumedPods != null ? consumedPods : DEFAULT_CONSUMED_PODS;
    }

    public Integer getConsumedMemory() {
        return consumedMemory != null ? consumedMemory : DEFAULT_CONSUMED_MEMORY;
    }

    public Integer getConsumedCpu() {
        return consumedCpu != null ? consumedCpu : DEFAULT_CONSUMED_CPU;
    }
}
