package net.geant.nmaas.externalservices.inventory.kubernetes;

import net.geant.nmaas.externalservices.inventory.kubernetes.entities.IngressControllerConfigOption;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.IngressResourceConfigOption;
import net.geant.nmaas.externalservices.inventory.kubernetes.model.KClusterExtNetworkView;

public interface KClusterIngressManager {

    IngressControllerConfigOption getControllerConfigOption();

    String getSupportedIngressClass();

    String getControllerChart();

    String getControllerChartArchive();

    IngressResourceConfigOption getResourceConfigOption();

    String getExternalServiceDomain();

    Boolean getTlsSupported();

    KClusterExtNetworkView reserveExternalNetwork(String domain);

    KClusterExtNetworkView getReservedExternalNetwork(String domain);

}
