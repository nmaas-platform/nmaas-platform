package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KubernetesChartView {

    private Long id;

    private String name;

    private String version;
}
