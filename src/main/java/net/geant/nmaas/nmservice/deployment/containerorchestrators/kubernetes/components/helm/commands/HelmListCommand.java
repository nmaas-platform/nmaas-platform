package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.commands;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmCommand;

import java.util.function.Predicate;

public class HelmListCommand extends HelmCommand {

    private static final String LIST = "list";
    private static final String LIST_OPTION = "--short";

    /**
     * Creates {@link HelmListCommand}
     *
     * @param helmVersion version of Helm in use
     * @param namespace namespace to install the release into
     * @param enableTls flag indicating if tls option should be added
     * @return complete command object
     */
    public static HelmListCommand command(String helmVersion, String namespace, boolean enableTls) {
        StringBuilder sb = new StringBuilder();
        sb.append(HELM).append(SPACE).append(LIST).append(SPACE).append(LIST_OPTION);
        if (HELM_VERSION_3.equals(helmVersion)) {
            sb.append(SPACE).append(OPTION_NAMESPACE).append(SPACE).append(namespace);
        }
        addTlsOptionIfRequired(helmVersion, enableTls, sb);
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
