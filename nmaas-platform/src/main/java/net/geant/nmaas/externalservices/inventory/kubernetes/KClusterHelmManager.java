package net.geant.nmaas.externalservices.inventory.kubernetes;

public interface KClusterHelmManager {

    String getHelmHostAddress();

    String getHelmHostSshUsername();

    Boolean getUseLocalChartArchives();

    String getHelmChartRepositoryName();

    String getHelmHostChartsDirectory();

}
