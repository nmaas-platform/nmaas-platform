package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import java.util.function.Predicate;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class HelmListCommand extends HelmCommand {

    private static final String LIST = "status";
    private static final String LIST_OPTION = "--short";

    /**
     * Creates {@link HelmListCommand}.
     *
     * @return complete command object
     */
    public static HelmListCommand command() {
        StringBuilder sb = new StringBuilder();
        sb.append(HELM).append(SPACE).append(LIST).append(SPACE).append(LIST_OPTION);
        return new HelmListCommand(sb.toString());
    }

    private HelmListCommand(String command) {
        this.command = command;
    }

    @Override
    public Predicate<String> isOutputCorrect() {
        return o -> !o.startsWith("Error");
    }

}
