package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import java.util.function.Predicate;

public class HelmUpgradeCommand extends HelmCommand {

    private static final String UPGRADE = "upgrade";

    /**
     * Creates {@link HelmUpgradeCommand} with provided custom input.
     *
     * @param releaseName release name
     * @param chartArchive complete path to the release chart archive
     * @return complete command object
     */
    static HelmUpgradeCommand commandWithArchive(String releaseName, String chartArchive) {
        if (releaseName == null || releaseName.isEmpty())
            throw new IllegalArgumentException("Name of the release can't be null or empty");
        if (chartArchive == null || chartArchive.isEmpty())
            throw new IllegalArgumentException("Path to chart archive can't be null or empty");
        StringBuilder sb = new StringBuilder();
        sb.append(HELM)
                .append(SPACE).append(UPGRADE)
                .append(SPACE).append(releaseName)
                .append(SPACE).append(chartArchive);
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
