package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkArgument;

@NoArgsConstructor
@AllArgsConstructor
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

    /**
     * Name of the deployment to be used by the Janitor to check if service was deployed successfully
     */
    private String mainDeploymentName;

    /**
     * The entity representing helm chart repository
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride( name = "name", column = @Column(name = "helm_chart_repository_name")),
            @AttributeOverride( name = "url",  column = @Column(name = "helm_chart_repository_url")),
    })
    private HelmChartRepositoryEmbeddable helmChartRepository = new HelmChartRepositoryEmbeddable();

    public KubernetesTemplate(KubernetesChart chart, String archive, String mainDeploymentName) {
        this.chart = chart;
        this.archive = archive;
        this.mainDeploymentName = mainDeploymentName;
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
        template.setMainDeploymentName(toCopy.getMainDeploymentName());
        template.setHelmChartRepository(toCopy.getHelmChartRepository());
        return template;
    }

    public void validate(){
        checkArgument(chart != null, "Kubernetes chart must be provided");
        checkArgument(StringUtils.isNotEmpty(chart.getName()), "You must provide chart name");
        checkArgument(StringUtils.isNotEmpty(chart.getVersion()), "You must provide chart version");
    }

    @Override
    public String toString() {
        return "KubernetesTemplate{" +
                "chart=" + chart +
                '}';
    }
}
