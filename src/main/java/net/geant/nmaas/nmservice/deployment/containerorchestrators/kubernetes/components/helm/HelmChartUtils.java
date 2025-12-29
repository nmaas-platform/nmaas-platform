package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.HelmChartRepositoryEmbeddable;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import org.springframework.util.StringUtils;

public class HelmChartUtils {

    private static final String OCI_PREFIX = "oci://";

    private static boolean isOciReference(String value) {
        return StringUtils.hasText(value) && value.trim().toLowerCase().startsWith(OCI_PREFIX);
    }

    public static boolean isOciChart(KubernetesTemplate template) {
        if (template == null) {
            return false;
        }
        if (template.getChart() != null && isOciReference(template.getChart().getName())) {
            return true;
        }
        HelmChartRepositoryEmbeddable repository = template.getHelmChartRepository();
        return repository != null && isOciReference(repository.getUrl());
    }
}

