package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import net.geant.nmaas.orchestration.entities.Identifier;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class HelmCommandPreparationTest {

    private static final String NAMESPACE = "nmaas";
    private static final Identifier DEPLOYMENT_ID = Identifier.newInstance("deploymentId");
    private static final String CHART_ARCHIVE_NAME = "/home/nmaas/charts/testapp-0.0.1";
    private static final String CORRECT_HELM_INSTALL_COMMAND_FIRST_PART =
            "helm install --name " + DEPLOYMENT_ID.value() + " --namespace " + NAMESPACE;
    private static final String CORRECT_HELM_INSTALL_COMMAND =
            CORRECT_HELM_INSTALL_COMMAND_FIRST_PART + " " + CHART_ARCHIVE_NAME;
    private static final String CORRECT_HELM_DELETE_COMMAND = "helm delete --purge " + DEPLOYMENT_ID.value();
    private static final String CORRECT_HELM_STATUS_COMMAND = "helm status " + DEPLOYMENT_ID.value();

    @Test
    public void shouldConstructInstallCommandWithNoArguments() {
        assertThat(HelmInstallCommand.command(NAMESPACE, DEPLOYMENT_ID.value(), null, CHART_ARCHIVE_NAME).asString(),
                equalTo(CORRECT_HELM_INSTALL_COMMAND));
    }

    @Test
    public void shouldConstructInstallCommandWithArguments() {
        Map<String, String> arguments = new HashMap<>();
        arguments.put(KubernetesManager.HELM_INSTALL_OPTION_PERSISTENCE_NAME, "persistenceName");
        arguments.put(KubernetesManager.HELM_INSTALL_OPTION_PERSISTENCE_STORAGE_CLASS, "storageClass");
        arguments.put(KubernetesManager.HELM_INSTALL_OPTION_NMAAS_CONFIG_REPOURL, "repoUrl");
        assertThat(HelmInstallCommand.command(NAMESPACE, DEPLOYMENT_ID.value(), arguments, CHART_ARCHIVE_NAME).asString(),
                allOf(containsString(CORRECT_HELM_INSTALL_COMMAND_FIRST_PART),
                        containsString("persistenceName"),
                        containsString("storageClass"),
                        containsString("repoUrl")));
    }

    @Test
    public void shouldConstructDeleteCommand() {
        assertThat(HelmDeleteCommand.command(DEPLOYMENT_ID.value()).asString(), equalTo(CORRECT_HELM_DELETE_COMMAND));
    }

    @Test
    public void shouldConstructStatusCommand() {
        assertThat(HelmStatusCommand.command(DEPLOYMENT_ID.value()).asString(), equalTo(CORRECT_HELM_STATUS_COMMAND));
    }

}
