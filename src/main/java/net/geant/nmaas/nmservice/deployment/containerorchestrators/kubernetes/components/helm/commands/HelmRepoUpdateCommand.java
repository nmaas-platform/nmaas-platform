package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.commands;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmCommand;

import java.util.function.Predicate;

public class HelmRepoUpdateCommand extends HelmCommand {

    private static final String REPO = "repo";
    private static final String UPDATE = "update";

    /**
     * Creates {@link HelmRepoUpdateCommand}.
     *
     * @return complete command object
     */
    public static HelmRepoUpdateCommand command() {
        StringBuilder sb = new StringBuilder();
        sb.append(HELM).append(SPACE).append(REPO).append(SPACE).append(UPDATE);
        return new HelmRepoUpdateCommand(sb.toString());
    }

    private HelmRepoUpdateCommand(String command) {
        this.command = command;
    }

    @Override
    public Predicate<String> isOutputCorrect() {
        return o -> o.contains("Update Complete");
    }

}
