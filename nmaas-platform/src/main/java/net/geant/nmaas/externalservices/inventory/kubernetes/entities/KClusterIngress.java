package net.geant.nmaas.externalservices.inventory.kubernetes.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Set of properties describing a Kubernetes cluster ingress handling
 */
@Getter
@Setter
@Entity
@Table(name="k_cluster_ingress")
public class KClusterIngress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Indicates if existing ingress controller should be used */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private IngressControllerConfigOption controllerConfigOption;

    /** Name of the ingress class handled by the existing ingress controller (required if useExistingController == true) */
    private String supportedIngressClass;

    /** Name of the chart to be downloaded from repository (required if useExistingController == false) */
    private String controllerChartName;

    /** Name of ingress controller helm chart archive (required if useExistingController == false) */
    private String controllerChartArchive;

    /** Indicates if and how ingress resources should be configured */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private IngressResourceConfigOption resourceConfigOption;

    /** Common part of the external service URL assigned to deployed services */
    private String externalServiceDomain;

    /** Indicates if TLS for ingress is supported */
    private Boolean tlsSupported;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private IngressCertificateConfigOption certificateConfigOption;

    private String issuerOrWildcardName;
}
