package net.geant.nmaas.externalservices.inventory.kubernetes.model;

import java.net.InetAddress;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
class KClusterHelmView {

    private Long id;

    private InetAddress helmHostAddress;

    private String helmHostSshUsername;

    private Boolean useLocalChartArchives;

    private String helmChartRepositoryName;

    private String helmHostChartsDirectory;
}
