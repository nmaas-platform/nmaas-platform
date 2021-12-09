package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.commands;

import com.google.common.base.Strings;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmCommand;

import java.util.function.Predicate;

public class HelmUpgradeCommand extends HelmCommand {

    private static final String UPGRADE = "upgrade";

    /**
     * Creates {@link HelmUpgradeCommand} with provided custom input
     *
     * @param helmVersion version of Helm in use
     * @param namespace namespace of the release
     * @param releaseName release name
     * @param chartName name of the target Helm chart
     * @param chartVersion version of the target Helm chart
     * @param enableTls flag indicating if tls option should be added
     * @return complete command object
     */
    public static HelmUpgradeCommand commandWithRepo(String helmVersion, String namespace, String releaseName, String chartName, String chartVersion, boolean enableTls) {
        if (!HELM_VERSION_3.equals(helmVersion)) {
            throw new IllegalArgumentException("Upgrades are not supported for Helm v2");
        }
        if (releaseName == null || releaseName.isEmpty()) {
            throw new IllegalArgumentException("Name of the release can't be null or empty");
        }
        if (Strings.isNullOrEmpty(chartName) || Strings.isNullOrEmpty(chartVersion)) {
            throw new IllegalArgumentException("Chart information can't be null or empty");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(HELM)
                .append(SPACE).append(UPGRADE)
                .append(SPACE).append(OPTION_NAMESPACE).append(SPACE).append(namespace)
                .append(SPACE).append(releaseName)
                .append(SPACE).append(chartName)
                .append(SPACE).append(OPTION_VERSION).append(SPACE).append(chartVersion);

        addTlsOptionIfRequired(helmVersion, enableTls, sb);
        return new HelmUpgradeCommand(sb.toString());
    }

    /**
     * Creates {@link HelmUpgradeCommand} with provided custom input
     *
     * @param helmVersion version of Helm in use
     * @param namespace namespace of the release
     * @param releaseName release name
     * @param chartArchive complete path to the release chart archive
     * @param enableTls flag indicating if tls option should be added
     * @return complete command object
     */
    public static HelmUpgradeCommand commandWithArchive(String helmVersion, String namespace, String releaseName, String chartArchive, boolean enableTls) {
        if (releaseName == null || releaseName.isEmpty()) {
            throw new IllegalArgumentException("Name of the release can't be null or empty");
        }
        if (chartArchive == null || chartArchive.isEmpty()) {
            throw new IllegalArgumentException("Path to chart archive can't be null or empty");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(HELM)
                .append(SPACE).append(UPGRADE)
                .append(SPACE).append(OPTION_NAMESPACE).append(SPACE).append(namespace)
                .append(SPACE).append(releaseName)
                .append(SPACE).append(chartArchive);
        addTlsOptionIfRequired(helmVersion, enableTls, sb);
        return new HelmUpgradeCommand(sb.toString());
    }

    private HelmUpgradeCommand(String command) {
        this.command = command;
    }

    @Override
    public Predicate<String> isOutputCorrect() {
        return o -> !o.startsWith("Error");
    }

}
