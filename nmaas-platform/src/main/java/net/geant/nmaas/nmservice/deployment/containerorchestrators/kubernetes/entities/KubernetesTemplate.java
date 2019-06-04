package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities;


import static com.google.common.base.Preconditions.checkArgument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.io.Serializable;
import org.apache.commons.lang.StringUtils;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class KubernetesTemplate implements Serializable {

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

    public void validate(){
        checkArgument(chart != null, "Chart cannot not be null");
        checkArgument(StringUtils.isNotEmpty(chart.getName()), "You must provide chart name");
        checkArgument(StringUtils.isNotEmpty(chart.getVersion()), "You must provide chart version");
    }

}
