package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.commands;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmCommand;

import java.util.function.Predicate;

public class HelmVersionCommand extends HelmCommand {

    private static final String VERSION = "version";

    /**
     * Creates {@link HelmVersionCommand}
     *
     * @param helmVersion version of Helm in use
     * @param enableTls flag indicating if tls option should be added
     * @return complete command object
     */
    public static HelmVersionCommand command(String helmVersion, boolean enableTls){
        StringBuilder sb = new StringBuilder();
        sb.append(HELM).append(SPACE).append(VERSION);
        if(HELM_VERSION_2.equals(helmVersion) && enableTls){
            sb.append(SPACE).append(TLS);
        }
        return new HelmVersionCommand(sb.toString());
    }

    private HelmVersionCommand(String command){
        this.command = command;
    }

    @Override
    public Predicate<String> isOutputCorrect() {
        return o -> !o.startsWith("Error");
    }
}
