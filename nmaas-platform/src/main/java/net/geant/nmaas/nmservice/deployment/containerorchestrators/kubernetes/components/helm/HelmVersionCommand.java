package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import java.util.function.Predicate;

public class HelmVersionCommand extends HelmCommand {

    private static final String VERSION = "version";

    static HelmVersionCommand command(boolean enableTls){
        StringBuilder sb = new StringBuilder();
        sb.append(HELM).append(SPACE).append(VERSION);
        if(enableTls){
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
