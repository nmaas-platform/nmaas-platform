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
    public static HelmStatusCommand command(String helmVersion, String namespace, String releaseName, boolean enableTls) {
        if (releaseName == null || releaseName.isEmpty())
            throw new IllegalArgumentException("Name of the release can't be null or empty");
        StringBuilder sb = new StringBuilder();
        sb.append(HELM).append(SPACE).append(STATUS).append(SPACE).append(releaseName);
        if (HELM_VERSION_3.equals(helmVersion)) {
            sb.append(SPACE).append(OPTION_NAMESPACE).append(SPACE).append(namespace);
        }
        if(enableTls){
            sb.append(SPACE).append(TLS);
        }
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
