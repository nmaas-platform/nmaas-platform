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
    protected static final String TLS = "--tls";

    protected static final String HELM_VERSION_2 = "v2";
    protected static final String HELM_VERSION_3 = "v3";

    protected String command;

    @Override
    public String asString() {
        return command;
    }

    protected static void addTlsOptionIfRequired(String helmVersion, boolean enableTls, StringBuilder sb) {
        if(HELM_VERSION_2.equals(helmVersion) && enableTls){
            sb.append(SPACE).append(TLS);
        }
    }

}
