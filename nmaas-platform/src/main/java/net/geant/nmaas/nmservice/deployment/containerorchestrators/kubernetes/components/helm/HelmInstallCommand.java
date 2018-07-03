package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class HelmInstallCommand extends HelmCommand {

    private static final String INSTALL = "install";

    /**
     * Creates {@link HelmInstallCommand} with provided custom input.
     *
     * @param namespace namespace to install the release into
     * @param releaseName release name
     * @param values a map of key - value pairs to customize the release installation
     * @param chartName chart name for download from repository
     * @param chartVersion chart version from download from repository
     * @return complete command object
     */
    static HelmInstallCommand commandWithRepo(String namespace, String releaseName, Map<String, String> values, String chartName, String chartVersion) {
        StringBuilder sb = buildBaseInstallCommand(namespace, releaseName, values);
        if (chartName == null || chartName.isEmpty()) {
            throw new IllegalArgumentException("Chart name can't be null or empty");
        }
        sb.append(SPACE).append(chartName);
        if (chartVersion != null && !chartVersion.isEmpty()) {
            sb.append(SPACE).append(OPTION_VERSION).append(SPACE).append(chartVersion);
        }
        return new HelmInstallCommand(sb.toString());
    }

    /**
     * Creates {@link HelmInstallCommand} with provided custom input and local chart archive.
     *
     * @param namespace namespace to install the release into
     * @param releaseName release name
     * @param values a map of key - value pairs to customize the release installation
     * @param chartArchive complete path to the release chart archive
     * @return complete command object
     */
    static HelmInstallCommand commandWithArchive(String namespace, String releaseName, Map<String, String> values, String chartArchive) {
        StringBuilder sb = buildBaseInstallCommand(namespace, releaseName, values);
        if (chartArchive == null || chartArchive.isEmpty()) {
            throw new IllegalArgumentException("Path to chart archive can't be null or empty");
        }
        sb.append(SPACE).append(chartArchive);
        return new HelmInstallCommand(sb.toString());
    }

    private static StringBuilder buildBaseInstallCommand(String namespace, String releaseName, Map<String, String> values) {
        if (releaseName == null || releaseName.isEmpty()) {
            throw new IllegalArgumentException("Name of the release can't be null or empty");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(HELM).append(SPACE).append(INSTALL).append(SPACE)
                .append(OPTION_NAME).append(SPACE).append(releaseName).append(SPACE)
                .append(OPTION_NAMESPACE).append(SPACE).append(namespace);
        if (values != null && !values.isEmpty()) {
            sb.append(SPACE).append(OPTION_SET).append(SPACE).append(commaSeparatedValuesString(values));
        }
        return sb;
    }

    private static String commaSeparatedValuesString(Map<String, String> values) {
        return values.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining(COMMA));
    }

    private HelmInstallCommand(String command) {
        this.command = command;
    }

    @Override
    public Predicate<String> isOutputCorrect() {
        return o -> !o.startsWith("Error");
    }

}
