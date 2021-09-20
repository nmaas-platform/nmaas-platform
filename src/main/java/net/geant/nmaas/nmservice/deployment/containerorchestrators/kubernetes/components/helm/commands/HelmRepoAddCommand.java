package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.commands;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmCommand;

import java.util.function.Predicate;

public class HelmRepoAddCommand extends HelmCommand {

    private static final String REPO = "repo";
    private static final String ADD = "add";

     /**
     * Creates {@link HelmRepoAddCommand}.
     *
     * @param repoName Name of Helm repository
     * @param repoUrl Url of the Helm repository
     * @return complete command object
     */
    public static HelmRepoAddCommand command(String repoName, String repoUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append(HELM).append(SPACE).append(REPO).append(SPACE).append(ADD)
                .append(SPACE).append(repoName)
                .append(SPACE).append(repoUrl);
        return new HelmRepoAddCommand(sb.toString());
    }

    private HelmRepoAddCommand(String command) {
        this.command = command;
    }

    @Override
    public Predicate<String> isOutputCorrect() {
        return o -> o.contains("has been added to your repositories");
    }

}
