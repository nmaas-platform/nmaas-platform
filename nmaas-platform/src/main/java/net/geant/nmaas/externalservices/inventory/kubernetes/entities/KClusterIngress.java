package net.geant.nmaas.externalservices.inventory.kubernetes.entities;

import javax.persistence.*;

/**
 * Set of properties describing a Kubernetes cluster ingress handling
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="k_cluster_ingress")
public class KClusterIngress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Indicates if existing ingress controller should be used */
    @Column(nullable = false)
    private Boolean useExistingController;

    /** Name of the ingress class handled by the existing ingress controller */
    private String supportedIngressClass;

    /** Name of the chart to be downloaded from repository */
    private String controllerChart;

    /** Name of ingress controller helm chart archive */
    private String controllerChartArchive;

    /** Indicates whether the ingress resource should be updated automatically */
    @Column(nullable = false)
    private Boolean configureIngress;

    /** Use existing ingress or ingress resource definition from the helm chart */
    private Boolean useExistingIngress;

    /** Common part of the external service URL assigned to deployed services */
    @Column(nullable = false)
    private String externalServiceDomain;

    /** Indicates if TLS for ingress is supported */
    @Column(nullable = false)
    private Boolean tlsSupported;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getUseExistingController() {
        return useExistingController;
    }

    public void setUseExistingController(Boolean useExistingController) {
        this.useExistingController = useExistingController;
    }

    public String getSupportedIngressClass() {
        return supportedIngressClass;
    }

    public void setSupportedIngressClass(String supportedIngressClass) {
        this.supportedIngressClass = supportedIngressClass;
    }

    public String getControllerChart() {
        return controllerChart;
    }

    public void setControllerChart(String controllerChart) {
        this.controllerChart = controllerChart;
    }

    public String getControllerChartArchive() {
        return controllerChartArchive;
    }

    public void setControllerChartArchive(String controllerChartArchive) {
        this.controllerChartArchive = controllerChartArchive;
    }

    public Boolean getConfigureIngress() {
        return configureIngress;
    }

    public void setConfigureIngress(Boolean configureIngress) {
        this.configureIngress = configureIngress;
    }

    public Boolean getUseExistingIngress() {
        return useExistingIngress;
    }

    public void setUseExistingIngress(Boolean useExistingIngress) {
        this.useExistingIngress = useExistingIngress;
    }

    public String getExternalServiceDomain() {
        return externalServiceDomain;
    }

    public void setExternalServiceDomain(String externalServiceDomain) {
        this.externalServiceDomain = externalServiceDomain;
    }

    public Boolean getTlsSupported() {
        return tlsSupported;
    }

    public void setTlsSupported(Boolean tlsSupported) {
        this.tlsSupported = tlsSupported;
    }
}
