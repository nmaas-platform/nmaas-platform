package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
public class KubernetesTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name of the helm chart to use from repository
     */
    private String chart;

    /**
     * The name of the helm chart archive to use from local directory
     */
    private String archive;

    public KubernetesTemplate() {
    }

    public KubernetesTemplate(String chart, String archive) {
        this.chart = chart;
        this.archive = archive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChart() {
        return chart;
    }

    public void setChart(String chart) {
        this.chart = chart;
    }

    public String getArchive() {
        return archive;
    }

    public void setArchive(String archive) {
        this.archive = archive;
    }

    public static KubernetesTemplate copy(KubernetesTemplate toCopy) {
        KubernetesTemplate template = new KubernetesTemplate();
        template.setChart(toCopy.getChart());
        template.setArchive(toCopy.getArchive());
        return template;
    }

}
