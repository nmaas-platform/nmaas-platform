package net.geant.nmaas.externalservices.kubernetes.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.externalservices.kubernetes.entities.IngressCertificateConfigOption;
import net.geant.nmaas.externalservices.kubernetes.entities.IngressControllerConfigOption;
import net.geant.nmaas.externalservices.kubernetes.entities.IngressResourceConfigOption;
import net.geant.nmaas.externalservices.kubernetes.entities.NamespaceConfigOption;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class KClusterView {

    private KClusterIngressView ingress;

    private KClusterDeploymentView deployment;

    @NoArgsConstructor
    @Getter
    @Setter
    public static class KClusterDeploymentView {

        private Long id;

        private NamespaceConfigOption namespaceConfigOption;

        private String defaultNamespace;

        private String defaultStorageClass;

        private String smtpServerHostname;

        private Integer smtpServerPort;

        private String smtpServerUsername;

        private String smtpServerPassword;

        private String smtpFromDefaultDomain;

        private Boolean forceDedicatedWorkers;

    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class KClusterIngressView {

        private Long id;

        private IngressControllerConfigOption controllerConfigOption;

        private String supportedIngressClass;

        private String publicIngressClass;

        private String controllerChartName;

        private String controllerChartArchive;

        private IngressResourceConfigOption resourceConfigOption;

        private String externalServiceDomain;

        private String publicServiceDomain;

        private Boolean tlsSupported;

        private IngressCertificateConfigOption certificateConfigOption;

        private String issuerOrWildcardName;

        private Boolean ingressPerDomain;

    }

}
