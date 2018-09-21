package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import java.util.function.Predicate;

public class HelmStatusCommand extends HelmCommand {

    private static final String STATUS = "status";

    /**
     * Creates {@link HelmStatusCommand} with provided custom input.
     *
     * @param releaseName release name
     * @return complete command object
     */
    public static HelmStatusCommand command(String releaseName) {
        if (releaseName == null || releaseName.isEmpty())
            throw new IllegalArgumentException("Name of the release can't be null or empty");
        StringBuilder sb = new StringBuilder();
        sb.append(HELM).append(SPACE).append(STATUS).append(SPACE).append(releaseName);
        return new HelmStatusCommand(sb.toString());
    }

    private HelmStatusCommand(String command) {
        this.command = command;
    }

    @Override
    public Predicate<String> isOutputCorrect() {
        return o -> !o.startsWith("Error");
    }

}
