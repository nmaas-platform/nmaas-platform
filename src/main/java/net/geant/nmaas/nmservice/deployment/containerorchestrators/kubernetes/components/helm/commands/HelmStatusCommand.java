package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.commands;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmCommand;

import java.util.function.Predicate;

public class HelmStatusCommand extends HelmCommand {

    private static final String STATUS = "status";

    /**
     * Creates {@link HelmStatusCommand} with provided custom input
     *
     * @param helmVersion version of Helm in use
     * @param namespace namespace with given release
     * @param releaseName release name
     * @param enableTls flag indicating if tls option should be added
     * @return complete command object
     */
    public static HelmStatusCommand command(String helmVersion, String namespace, String releaseName, boolean enableTls) {
        if (releaseName == null || releaseName.isEmpty()) {
            throw new IllegalArgumentException("Name of the release can't be null or empty");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(HELM).append(SPACE).append(STATUS).append(SPACE).append(releaseName);
        if (HELM_VERSION_3.equals(helmVersion)) {
            sb.append(SPACE).append(OPTION_NAMESPACE).append(SPACE).append(namespace);
        }
        addTlsOptionIfRequired(helmVersion, enableTls, sb);
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
