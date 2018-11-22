package net.geant.nmaas.externalservices.inventory.kubernetes.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.IngressCertificateConfigOption;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.IngressControllerConfigOption;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.IngressResourceConfigOption;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
class KClusterIngressView {

    private Long id;

    private IngressControllerConfigOption controllerConfigOption;

    private String supportedIngressClass;

    private String controllerChartName;

    private String controllerChartArchive;

    private IngressResourceConfigOption resourceConfigOption;

    private String externalServiceDomain;

    private Boolean tlsSupported;

    private IngressCertificateConfigOption certificateConfigOption;
}
