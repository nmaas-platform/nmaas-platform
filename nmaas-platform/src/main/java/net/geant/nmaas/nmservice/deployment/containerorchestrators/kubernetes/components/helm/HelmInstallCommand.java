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
     * @param chartArchive complete path to the release chart archive
     * @return complete command object
     */
    public static HelmInstallCommand command(String namespace, String releaseName, Map<String, String> values, String chartArchive) {
        if (releaseName == null || releaseName.isEmpty())
            throw new IllegalArgumentException("Name of the release can't be null or empty");
        if (chartArchive == null || chartArchive.isEmpty())
            throw new IllegalArgumentException("Path to chart archive can't be null or empty");
        StringBuilder sb = new StringBuilder();
        sb.append(HELM).append(SPACE).append(INSTALL).append(SPACE)
                .append(OPTION_NAME).append(SPACE).append(releaseName).append(SPACE)
                .append(OPTION_NAMESPACE).append(SPACE).append(namespace);
        if (values != null && !values.isEmpty())
            sb.append(SPACE).append(OPTION_SET).append(SPACE).append(commaSeparatedValuesString(values));
        sb.append(SPACE).append(chartArchive);
        return new HelmInstallCommand(sb.toString());
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
