package net.geant.nmaas.externalservices.kubernetes.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KClusterIngress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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


    @OneToOne(mappedBy = "ingress", cascade = CascadeType.ALL)
    private ClusterManager clusterManager;
}
