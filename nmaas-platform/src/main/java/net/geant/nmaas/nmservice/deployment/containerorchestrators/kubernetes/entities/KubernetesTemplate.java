package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities;


import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
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

    public KubernetesTemplate(KubernetesChart chart, String archive) {
        this.chart = chart;
        this.archive = archive;
    }

    public KubernetesTemplate(String chartName, String chartVersion, String archive) {
        this.chart = new KubernetesChart(chartName, chartVersion);
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
