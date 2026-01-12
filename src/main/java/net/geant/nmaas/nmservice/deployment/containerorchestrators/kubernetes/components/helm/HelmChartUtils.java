package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import org.springframework.util.StringUtils;

public class HelmChartUtils {

    private static final String OCI_PREFIX = "oci://";

    public static boolean isOciChart(KubernetesTemplate template) {
        if (template == null || template.getChart() == null) {
            return false;
        }
        String chartName = template.getChart().getName();
        return StringUtils.hasText(chartName) && chartName.trim().toLowerCase().startsWith(OCI_PREFIX);
    }
}

