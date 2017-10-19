package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import java.util.function.Predicate;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class HelmStatusCommand extends HelmCommand {

    private static final String STATUS = "status";

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
