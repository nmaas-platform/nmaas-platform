package net.geant.nmaas.externalservices.kubernetes.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "k_cluster_ingress")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KClusterIngress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private IngressControllerConfigOption controllerConfigOption;

    private String supportedIngressClass;

    private String publicIngressClass;

    private String controllerChartName;

    private String controllerChartArchive;

    @Enumerated(EnumType.STRING)
    private IngressResourceConfigOption resourceConfigOption;

    private String externalServiceDomain;

    private String publicServiceDomain;

    private Boolean tlsSupported;

    @Enumerated(EnumType.STRING)
    private IngressCertificateConfigOption certificateConfigOption;

    private String issuerOrWildcardName;

    private Boolean ingressPerDomain;

    public String getExternalServiceDomain() {
        return this.getExternalServiceDomain();
    }

}