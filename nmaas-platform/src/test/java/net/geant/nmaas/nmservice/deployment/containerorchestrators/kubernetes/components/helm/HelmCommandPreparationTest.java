package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class HelmCommandPreparationTest {

    private static final String NAMESPACE = "nmaas";
    private static final String RELEASE_NAME = "releaseName";
    private static final String CHART_ARCHIVE_NAME = "/home/nmaas/charts/testapp-0.0.1.tgz";
    private static final String CHART_NAME_WITH_REPO = "test-repo/testapp";
    private static final String CHART_VERSION = "0.0.1";
    private static final String CORRECT_HELM_INSTALL_COMMAND_FIRST_PART =
            "helm install --name " + RELEASE_NAME + " --namespace " + NAMESPACE;
    private static final String CORRECT_HELM_INSTALL_COMMAND_USING_LOCAL_CHART_ARCHIVE =
            CORRECT_HELM_INSTALL_COMMAND_FIRST_PART + " " + CHART_ARCHIVE_NAME;
    private static final String CORRECT_HELM_INSTALL_COMMAND_USING_CHART_FROM_REPO =
            CORRECT_HELM_INSTALL_COMMAND_FIRST_PART + " " + CHART_NAME_WITH_REPO;
    private static final String CORRECT_HELM_INSTALL_COMMAND_USING_CHART_FROM_REPO_WITH_VERSION =
            CORRECT_HELM_INSTALL_COMMAND_FIRST_PART + " " + CHART_NAME_WITH_REPO + " --version " + CHART_VERSION;
    private static final String CORRECT_HELM_DELETE_COMMAND = "helm delete --purge " + RELEASE_NAME;
    private static final String CORRECT_HELM_STATUS_COMMAND = "helm status " + RELEASE_NAME;
    private static final String CORRECT_HELM_UPGRADE_COMMAND =
            "helm upgrade " + RELEASE_NAME + " " + CHART_ARCHIVE_NAME;
    private static final String CORRECT_HELM_VERSION_COMMAND = "helm version";
    private static final String TLS = " --tls";
    private static final String CORRECT_HELM_REPO_UPDATE_COMMAND = "helm repo update";

    @Test
    public void shouldConstructInstallCommandUsingLocalChartArchiveWithNoArgumentsWithDisabledTls() {
        assertThat(HelmInstallCommand.commandWithArchive(NAMESPACE, RELEASE_NAME, null, CHART_ARCHIVE_NAME, false).asString(),
                equalTo(CORRECT_HELM_INSTALL_COMMAND_USING_LOCAL_CHART_ARCHIVE));
    }

    @Test
    public void shouldConstructInstallCommandUsingLocalChartArchiveWithNoArgumentsWithEnabledTls() {
        assertThat(HelmInstallCommand.commandWithArchive(NAMESPACE, RELEASE_NAME, null, CHART_ARCHIVE_NAME, true).asString(),
                equalTo(CORRECT_HELM_INSTALL_COMMAND_USING_LOCAL_CHART_ARCHIVE + TLS));
    }

    @Test
    public void shouldConstructInstallCommandUsingChartFromRepoWithNoArgumentsWithDisabledTls() {
        assertThat(
                HelmInstallCommand.commandWithRepo(
                        NAMESPACE,
                        RELEASE_NAME,
                        null,
                        CHART_NAME_WITH_REPO,
                        null, false).asString(),
                equalTo(CORRECT_HELM_INSTALL_COMMAND_USING_CHART_FROM_REPO));
        assertThat(
                HelmInstallCommand.commandWithRepo(
                        NAMESPACE,
                        RELEASE_NAME,
                        null,
                        CHART_NAME_WITH_REPO,
                        CHART_VERSION, false).asString(),
                equalTo(CORRECT_HELM_INSTALL_COMMAND_USING_CHART_FROM_REPO_WITH_VERSION));
    }

    @Test
    public void shouldConstructInstallCommandUsingChartFromRepoWithNoArgumentsWithEnabledTls() {
        assertThat(
                HelmInstallCommand.commandWithRepo(
                        NAMESPACE,
                        RELEASE_NAME,
                        null,
                        CHART_NAME_WITH_REPO,
                        null, true).asString(),
                equalTo(CORRECT_HELM_INSTALL_COMMAND_USING_CHART_FROM_REPO + TLS));
        assertThat(
                HelmInstallCommand.commandWithRepo(
                        NAMESPACE,
                        RELEASE_NAME,
                        null,
                        CHART_NAME_WITH_REPO,
                        CHART_VERSION, true).asString(),
                equalTo(CORRECT_HELM_INSTALL_COMMAND_USING_CHART_FROM_REPO_WITH_VERSION + TLS));
    }

    @Test
    public void shouldConstructInstallCommandWithArgumentsWithDisabledTls() {
        Map<String, String> arguments = new HashMap<>();
        arguments.put("persistence.name", "testPersistenceName");
        arguments.put("persistence.storageClass", "testStorageClass");
        assertThat(HelmInstallCommand.commandWithArchive(NAMESPACE, RELEASE_NAME, arguments, CHART_ARCHIVE_NAME, false).asString(),
                allOf(containsString(CORRECT_HELM_INSTALL_COMMAND_FIRST_PART),
                        containsString("testPersistenceName"),
                        containsString("testStorageClass")));
    }

    @Test
    public void shouldConstructInstallCommandWithArgumentsWithEnabledTls() {
        Map<String, String> arguments = new HashMap<>();
        arguments.put("persistence.name", "testPersistenceName");
        arguments.put("persistence.storageClass", "testStorageClass");
        assertThat(HelmInstallCommand.commandWithArchive(NAMESPACE, RELEASE_NAME, arguments, CHART_ARCHIVE_NAME, true).asString(),
                allOf(containsString(CORRECT_HELM_INSTALL_COMMAND_FIRST_PART),
                        containsString("testPersistenceName"),
                        containsString("testStorageClass"),
                        containsString(TLS)));
    }

    @Test
    public void shouldConstructProperListOfInstallValues() {
        Map<String, String> mapOfValues = new HashMap<>();
        mapOfValues.put("key1", "value1");
        mapOfValues.put("key2=%VALUE%", "value2");
        assertThat(HelmInstallCommand.commaSeparatedValuesString(mapOfValues), equalTo("key1=value1,key2=value2"));
    }

    @Test
    public void shouldConstructDeleteCommandWithDisabledTls() {
        assertThat(HelmDeleteCommand.command(RELEASE_NAME, false).asString(), equalTo(CORRECT_HELM_DELETE_COMMAND));
    }

    @Test
    public void shouldConstructDeleteCommandWithEnabledTls() {
        assertThat(HelmDeleteCommand.command(RELEASE_NAME, true).asString(), equalTo(CORRECT_HELM_DELETE_COMMAND + TLS));
    }

    @Test
    public void shouldConstructStatusCommandWithDisabledTls() {
        assertThat(HelmStatusCommand.command(RELEASE_NAME, false).asString(), equalTo(CORRECT_HELM_STATUS_COMMAND));
    }

    @Test
    public void shouldConstructStatusCommandWithEnabledTls() {
        assertThat(HelmStatusCommand.command(RELEASE_NAME, true).asString(), equalTo(CORRECT_HELM_STATUS_COMMAND + TLS));
    }

    @Test
    public void shouldConstructUpgradeCommandWithDisabledTls() {
        assertThat(HelmUpgradeCommand.commandWithArchive(RELEASE_NAME, CHART_ARCHIVE_NAME, false).asString(),
                equalTo(CORRECT_HELM_UPGRADE_COMMAND));
    }

    @Test
    public void shouldConstructUpgradeCommandWithEnabledTls() {
        assertThat(HelmUpgradeCommand.commandWithArchive(RELEASE_NAME, CHART_ARCHIVE_NAME, true).asString(),
                equalTo(CORRECT_HELM_UPGRADE_COMMAND + TLS));
    }

    @Test
    public void shouldConstructVersionCommandWithDisabledTls() {
        assertThat(HelmVersionCommand.command(false).asString(), equalTo(CORRECT_HELM_VERSION_COMMAND));
    }

    @Test
    public void shouldConstructVersionCommandWithEnabledTls() {
        assertThat(HelmVersionCommand.command(true).asString(), equalTo(CORRECT_HELM_VERSION_COMMAND + TLS));
    }

    @Test
    public void shouldConstructRepoUpdateCommand() {
        assertThat(HelmRepoUpdateCommand.command().asString(), equalTo(CORRECT_HELM_REPO_UPDATE_COMMAND));
    }

}
