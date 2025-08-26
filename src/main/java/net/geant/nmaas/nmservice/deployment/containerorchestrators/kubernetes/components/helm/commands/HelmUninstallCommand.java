package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.commands;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmCommand;

import java.util.function.Predicate;

public class HelmUninstallCommand extends HelmCommand {

    private static final String UNINSTALL = "uninstall";

    /**
     * Creates {@link HelmUninstallCommand} with provided custom input.
     *
     * @param namespace namespace with given release
     * @param releaseName release name
     * @param kubeConfigPath path to custom kubeConfig file (optional)
     * @return complete command object
     */
    public static HelmUninstallCommand command(String namespace, String releaseName, String kubeConfigPath) {
        if (releaseName == null || releaseName.isEmpty()) {
            throw new IllegalArgumentException("Name of the release can't be null or empty");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(HELM)
                .append(SPACE).append(UNINSTALL)
                .append(SPACE).append(releaseName)
                .append(SPACE).append(OPTION_NAMESPACE).append(SPACE).append(namespace);
        if (kubeConfigPath != null && !kubeConfigPath.isEmpty()) {
            sb.append(SPACE).append(OPTION_KUBECONFIG).append(SPACE).append(kubeConfigPath);
        }
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
