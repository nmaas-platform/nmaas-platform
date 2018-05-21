package net.geant.nmaas.externalservices.inventory.kubernetes;

import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KClusterExtNetworkView;
import net.geant.nmaas.externalservices.inventory.kubernetes.exceptions.ExternalNetworkNotFoundException;

public interface KClusterIngressManager {

    Boolean getUseExistingController();

    String getControllerChartArchive();

    Boolean getUseExistingIngress();

    String getExternalServiceDomain();

    KClusterExtNetworkView reserveExternalNetwork(String domain) throws ExternalNetworkNotFoundException;

    KClusterExtNetworkView getReservedExternalNetwork(String domain) throws ExternalNetworkNotFoundException;

}
