package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KubernetesTemplateView {

    private Long id;

    private KubernetesChartView chart = new KubernetesChartView();

    private String archive;

    private String mainDeploymentName;

    @Valid
    private HelmChartRepositoryView helmChartRepository = new HelmChartRepositoryView();

}
