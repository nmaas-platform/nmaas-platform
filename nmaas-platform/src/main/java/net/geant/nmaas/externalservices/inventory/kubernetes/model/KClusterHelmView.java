package net.geant.nmaas.externalservices.inventory.kubernetes.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.InetAddress;

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

    private Boolean enableTls;
}
