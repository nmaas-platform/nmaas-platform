package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import java.util.function.Predicate;

public class HelmUninstallCommand extends HelmCommand {

    private static final String UNINSTALL = "uninstall";

    /**
     * Creates {@link HelmUninstallCommand} with provided custom input.
     *
     * @param releaseName release name
     * @return complete command object
     */
    public static HelmUninstallCommand command(String namespace, String releaseName) {
        if (releaseName == null || releaseName.isEmpty())
            throw new IllegalArgumentException("Name of the release can't be null or empty");
        StringBuilder sb = new StringBuilder();
        sb.append(HELM)
                .append(SPACE).append(UNINSTALL)
                .append(SPACE).append(releaseName)
                .append(SPACE).append(OPTION_NAMESPACE).append(SPACE).append(namespace);
        return new HelmUninstallCommand(sb.toString());
    }

    private HelmUninstallCommand(String command) {
        this.command = command;
    }

    @Override
    public Predicate<String> isOutputCorrect() {
        return o -> !o.startsWith("Error");
    }

}
