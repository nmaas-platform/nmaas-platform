package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.commands;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmCommand;

import java.util.function.Predicate;

public class HelmUpgradeCommand extends HelmCommand {

    private static final String UPGRADE = "upgrade";

    /**
     * Creates {@link HelmUpgradeCommand} with provided custom input
     *
     * @param helmVersion version of Helm in use
     * @param releaseName release name
     * @param chartArchive complete path to the release chart archive
     * @param enableTls flag indicating if tls option should be added
     * @return complete command object
     */
    public static HelmUpgradeCommand commandWithArchive(String helmVersion, String releaseName, String chartArchive, boolean enableTls) {
        if (releaseName == null || releaseName.isEmpty()) {
            throw new IllegalArgumentException("Name of the release can't be null or empty");
        }
        if (chartArchive == null || chartArchive.isEmpty()) {
            throw new IllegalArgumentException("Path to chart archive can't be null or empty");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(HELM)
                .append(SPACE).append(UPGRADE)
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
