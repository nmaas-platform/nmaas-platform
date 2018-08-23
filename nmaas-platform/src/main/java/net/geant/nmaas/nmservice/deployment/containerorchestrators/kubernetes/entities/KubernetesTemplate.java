package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities;

import javax.persistence.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
public class KubernetesTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The helm chart to use from repository
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private KubernetesChart chart;

    /**
     * The name of the helm chart archive to use from local directory
     */
    private String archive;

    public KubernetesTemplate() {
    }

    public KubernetesTemplate(KubernetesChart chart, String archive) {
        this.chart = chart;
        this.archive = archive;
    }

    public KubernetesTemplate(String chartName, String chartVersion, String archive) {
        this.chart = new KubernetesChart(chartName, chartVersion);
        this.archive = archive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public KubernetesChart getChart() {
        return chart;
    }

    private void setChart(KubernetesChart chart) {
        this.chart = chart;
    }

    public String getArchive() {
        return archive;
    }

    private void setArchive(String archive) {
        this.archive = archive;
    }

    public static KubernetesTemplate copy(KubernetesTemplate toCopy) {
        KubernetesTemplate template = new KubernetesTemplate();
        if (toCopy.getChart() != null) {
            template.setChart(KubernetesChart.copy(toCopy.getChart()));
        }
        template.setArchive(toCopy.getArchive());
        return template;
    }

}
