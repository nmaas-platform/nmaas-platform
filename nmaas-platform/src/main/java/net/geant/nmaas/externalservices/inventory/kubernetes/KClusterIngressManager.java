package net.geant.nmaas.externalservices.inventory.kubernetes;

import net.geant.nmaas.externalservices.inventory.kubernetes.entities.IngressCertificateConfigOption;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.IngressControllerConfigOption;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.IngressResourceConfigOption;
import net.geant.nmaas.externalservices.inventory.kubernetes.model.KClusterExtNetworkView;

public interface KClusterIngressManager {

    IngressControllerConfigOption getControllerConfigOption();

    String getSupportedIngressClass();

    String getPublicIngressClass();

    String getControllerChart();

    String getControllerChartArchive();

    IngressResourceConfigOption getResourceConfigOption();

    String getExternalServiceDomain();

    String getExternalServiceDomain(String codename);

    String getPublicServiceDomain();

    Boolean getTlsSupported();

    Boolean getIngressPerDomain();

    IngressCertificateConfigOption getCertificateConfigOption();

    String getIssuerOrWildcardName();

    KClusterExtNetworkView reserveExternalNetwork(String domain);

    KClusterExtNetworkView getReservedExternalNetwork(String domain);

}
