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

    /** Use existing ingress controller */
    @Column(nullable = false)
    private Boolean useExistingController;

    /** Name of ingress controller helm chart archive */
    private String controllerChartArchive;

    /** Use existing ingress or ingress resource definition from the helm chart */
    @Column(nullable = false)
    private Boolean useExistingIngress;

    /** Common part of the external service URL assigned to deployed services */
    @Column(nullable = false)
    private String externalServiceDomain;

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

    public String getControllerChartArchive() {
        return controllerChartArchive;
    }

    public void setControllerChartArchive(String controllerChartArchive) {
        this.controllerChartArchive = controllerChartArchive;
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
}
