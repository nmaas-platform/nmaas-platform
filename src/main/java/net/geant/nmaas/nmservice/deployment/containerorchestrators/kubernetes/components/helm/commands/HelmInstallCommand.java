package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.commands;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmCommand;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class HelmInstallCommand extends HelmCommand {

    private static final String INSTALL = "install";
    public static final String TEXT_TO_REPLACE_WITH_VALUE = "%VALUE%";

    /**
     * Creates {@link HelmInstallCommand} with provided custom input
     *
     * @param helmVersion  version of Helm in use
     * @param namespace    namespace to install the release into
     * @param releaseName  release name
     * @param values       a map of key - value pairs to customize the release installation
     * @param chartName    chart name for download from repository
     * @param chartVersion chart version from download from repository
     * @param enableTls    flag indicating if tls option should be added
     * @return complete command object
     */
    public static HelmInstallCommand commandWithRepo(String helmVersion, String namespace, String releaseName, Map<String, String> values, String chartName, String chartVersion, boolean enableTls, String kubeConfigPath) {
        StringBuilder sb = buildBaseInstallCommand(helmVersion, namespace, releaseName, values);

        if(kubeConfigPath != null && !kubeConfigPath.isEmpty()) {
            sb.append(SPACE).append(OPTION_KUBECONFIG).append(SPACE).append(kubeConfigPath);
        }
        if (chartName == null || chartName.isEmpty()) {
            throw new IllegalArgumentException("Chart name can't be null or empty");
        }
        sb.append(SPACE).append(chartName);
        if (chartVersion != null && !chartVersion.isEmpty()) {
            sb.append(SPACE).append(OPTION_VERSION).append(SPACE).append(chartVersion);
        }
        addTlsOptionIfRequired(helmVersion, enableTls, sb);

        return new HelmInstallCommand(sb.toString());
    }

    private static StringBuilder buildBaseInstallCommand(String helmVersion, String namespace, String releaseName, Map<String, String> values) {
        if (releaseName == null || releaseName.isEmpty()) {
            throw new IllegalArgumentException("Name of the release can't be null nor empty");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(HELM).append(SPACE).append(INSTALL).append(SPACE);
        if (HELM_VERSION_2.equals(helmVersion)) {
            sb.append(OPTION_NAME).append(SPACE);
        }
        sb.append(releaseName).append(SPACE).append(OPTION_NAMESPACE).append(SPACE).append(namespace);
        if (values != null && !values.isEmpty()) {
            sb.append(SPACE).append(OPTION_SET).append(SPACE).append(commaSeparatedValuesString(values));
        }
        return sb;
    }

    public static String commaSeparatedValuesString(Map<String, String> values) {
        values.forEach((key, value) -> log.info("{} {}", key, value));
        return values.entrySet().stream()
                .map(entry -> {
                    if (entry.getKey().contains(TEXT_TO_REPLACE_WITH_VALUE)) {
                        return entry.getKey().replace(TEXT_TO_REPLACE_WITH_VALUE, entry.getValue());
                    } else {
                        return entry.getKey() + "=" + entry.getValue();
                    }
                }).collect(Collectors.joining(COMMA));
    }

    private HelmInstallCommand(String command) {
        this.command = command;
    }

    @Override
    public Predicate<String> isOutputCorrect() {
        return o -> !o.startsWith("Error");
    }

}
