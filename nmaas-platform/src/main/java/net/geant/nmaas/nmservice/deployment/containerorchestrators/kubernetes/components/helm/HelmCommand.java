package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import net.geant.nmaas.utils.ssh.Command;

public abstract class HelmCommand implements Command {

    protected static final String HELM = "helm";
    protected static final String SPACE = " ";
    protected static final String COMMA = ",";
    protected static final String OPTION_SET = "--set";
    protected static final String OPTION_NAMESPACE = "--namespace";
    protected static final String OPTION_NAME = "--name";
    protected static final String OPTION_VERSION = "--version";

    protected String command;

    @Override
    public String asString() {
        return command;
    }

}
