package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import java.util.function.Predicate;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class HelmDeleteCommand extends HelmCommand {

    private static final String DELETE = "delete";
    private static final String OPTION_PURGE = "--purge";

    /**
     * Creates {@link HelmDeleteCommand} with provided custom input.
     *
     * @param releaseName release name
     * @return complete command object
     */
    public static HelmDeleteCommand command(String releaseName) {
        if (releaseName == null || releaseName.isEmpty())
            throw new IllegalArgumentException("Name of the release can't be null or empty");
        StringBuilder sb = new StringBuilder();
        sb.append(HELM).append(SPACE).append(DELETE).append(SPACE).append(OPTION_PURGE).append(SPACE).append(releaseName);
        return new HelmDeleteCommand(sb.toString());
    }

    private HelmDeleteCommand(String command) {
        this.command = command;
    }

    @Override
    public Predicate<String> isOutputCorrect() {
        return o -> !o.startsWith("Error");
    }

}
