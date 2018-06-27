package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

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
    private static final String CHART_ARCHIVE_NAME = "/home/nmaas/charts/testapp-0.0.1.tgz";
    private static final String CHART_NAME_WITH_REPO = "test-repo/testapp";
    private static final String CHART_VERSION = "0.0.1";
    private static final String CORRECT_HELM_INSTALL_COMMAND_FIRST_PART =
            "helm install --name " + DEPLOYMENT_ID.value() + " --namespace " + NAMESPACE;
    private static final String CORRECT_HELM_INSTALL_COMMAND_USING_LOCAL_CHART_ARCHIVE =
            CORRECT_HELM_INSTALL_COMMAND_FIRST_PART + " " + CHART_ARCHIVE_NAME;
    private static final String CORRECT_HELM_INSTALL_COMMAND_USING_CHART_FROM_REPO =
            CORRECT_HELM_INSTALL_COMMAND_FIRST_PART + " " + CHART_NAME_WITH_REPO;
    private static final String CORRECT_HELM_INSTALL_COMMAND_USING_CHART_FROM_REPO_WITH_VERSION =
            CORRECT_HELM_INSTALL_COMMAND_FIRST_PART + " " + CHART_NAME_WITH_REPO + " --version " + CHART_VERSION;
    private static final String CORRECT_HELM_DELETE_COMMAND = "helm delete --purge " + DEPLOYMENT_ID.value();
    private static final String CORRECT_HELM_STATUS_COMMAND = "helm status " + DEPLOYMENT_ID.value();
    private static final String CORRECT_HELM_UPGRADE_COMMAND =
            "helm upgrade " + DEPLOYMENT_ID.value() + " " + CHART_ARCHIVE_NAME;

    @Test
    public void shouldConstructInstallCommandUsingLocalChartArchiveWithNoArguments() {
        assertThat(HelmInstallCommand.commandWithArchive(NAMESPACE, DEPLOYMENT_ID.value(), null, CHART_ARCHIVE_NAME).asString(),
                equalTo(CORRECT_HELM_INSTALL_COMMAND_USING_LOCAL_CHART_ARCHIVE));
    }

    @Test
    public void shouldConstructInstallCommandUsingChartFromRepoWithNoArguments() {
        assertThat(
                HelmInstallCommand.commandWithRepo(
                        NAMESPACE,
                        DEPLOYMENT_ID.value(),
                        null,
                        CHART_NAME_WITH_REPO,
                        null).asString(),
                equalTo(CORRECT_HELM_INSTALL_COMMAND_USING_CHART_FROM_REPO));
        assertThat(
                HelmInstallCommand.commandWithRepo(
                        NAMESPACE,
                        DEPLOYMENT_ID.value(),
                        null,
                        CHART_NAME_WITH_REPO,
                        CHART_VERSION).asString(),
                equalTo(CORRECT_HELM_INSTALL_COMMAND_USING_CHART_FROM_REPO_WITH_VERSION));
    }

    @Test
    public void shouldConstructInstallCommandWithArguments() {
        Map<String, String> arguments = new HashMap<>();
        arguments.put(HelmKServiceManager.HELM_INSTALL_OPTION_PERSISTENCE_NAME, "persistenceName");
        arguments.put(HelmKServiceManager.HELM_INSTALL_OPTION_PERSISTENCE_STORAGE_CLASS, "storageClass");
        arguments.put(HelmKServiceManager.HELM_INSTALL_OPTION_NMAAS_CONFIG_ACTION, "clone");
        arguments.put(HelmKServiceManager.HELM_INSTALL_OPTION_NMAAS_CONFIG_REPOURL, "repoUrl");
        assertThat(HelmInstallCommand.commandWithArchive(NAMESPACE, DEPLOYMENT_ID.value(), arguments, CHART_ARCHIVE_NAME).asString(),
                allOf(containsString(CORRECT_HELM_INSTALL_COMMAND_FIRST_PART),
                        containsString("persistenceName"),
                        containsString("storageClass"),
                        containsString("clone"),
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

    @Test
    public void shouldConstructUpgradeCommand() {
        assertThat(HelmUpgradeCommand.command(DEPLOYMENT_ID.value(), CHART_ARCHIVE_NAME).asString(),
                equalTo(CORRECT_HELM_UPGRADE_COMMAND));
    }

}
