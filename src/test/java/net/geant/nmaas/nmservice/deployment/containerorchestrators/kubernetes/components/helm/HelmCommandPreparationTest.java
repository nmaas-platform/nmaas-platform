package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.commands.HelmDeleteCommand;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.commands.HelmInstallCommand;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.commands.HelmRepoAddCommand;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.commands.HelmRepoUpdateCommand;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.commands.HelmStatusCommand;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.commands.HelmUpgradeCommand;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.commands.HelmVersionCommand;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmCommand.HELM_VERSION_2;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class HelmCommandPreparationTest {

    private static final String NAMESPACE = "nmaas";
    private static final String RELEASE_NAME = "releaseName";
    private static final String CHART_ARCHIVE_NAME = "/home/nmaas/charts/testapp-0.0.1.tgz";
    private static final String CHART_NAME_WITH_REPO = "test-repo/testapp";
    private static final String CHART_REPO_NAME = "test-repo";
    private static final String CHART_REPO_URL = "https://test-repo.eu";
    private static final String CHART_VERSION = "0.0.1";
    private static final String CORRECT_HELM_INSTALL_COMMAND_FIRST_PART =
            "helm install --name " + RELEASE_NAME + " --namespace " + NAMESPACE;
    private static final String CORRECT_HELM_INSTALL_COMMAND_FOR_V3_FIRST_PART =
            "helm install " + RELEASE_NAME + " --namespace " + NAMESPACE;
    private static final String CORRECT_HELM_INSTALL_COMMAND_USING_LOCAL_CHART_ARCHIVE =
            CORRECT_HELM_INSTALL_COMMAND_FIRST_PART + " " + CHART_ARCHIVE_NAME;
    private static final String CORRECT_HELM_INSTALL_COMMAND_USING_CHART_FROM_REPO =
            CORRECT_HELM_INSTALL_COMMAND_FIRST_PART + " " + CHART_NAME_WITH_REPO;
    private static final String CORRECT_HELM_INSTALL_COMMAND_FOR_v3_USING_CHART_FROM_REPO =
            CORRECT_HELM_INSTALL_COMMAND_FOR_V3_FIRST_PART + " " + CHART_NAME_WITH_REPO;
    private static final String CORRECT_HELM_INSTALL_COMMAND_USING_CHART_FROM_REPO_WITH_VERSION =
            CORRECT_HELM_INSTALL_COMMAND_FIRST_PART + " " + CHART_NAME_WITH_REPO + " --version " + CHART_VERSION;
    private static final String CORRECT_HELM_INSTALL_COMMAND_FOR_V3_USING_CHART_FROM_REPO_WITH_VERSION =
            CORRECT_HELM_INSTALL_COMMAND_FOR_V3_FIRST_PART + " " + CHART_NAME_WITH_REPO + " --version " + CHART_VERSION;
    private static final String CORRECT_HELM_DELETE_COMMAND = "helm delete --purge " + RELEASE_NAME;
    private static final String CORRECT_HELM_STATUS_COMMAND = "helm status " + RELEASE_NAME;
    private static final String CORRECT_HELM_STATUS_COMMAND_FOR_V3 = "helm status " + RELEASE_NAME + " --namespace " + NAMESPACE;
    private static final String CORRECT_HELM_UPGRADE_COMMAND =
            "helm upgrade " + RELEASE_NAME + " " + CHART_ARCHIVE_NAME;
    private static final String CORRECT_HELM_VERSION_COMMAND = "helm version";
    private static final String TLS = " --tls";
    private static final String CORRECT_HELM_REPO_UPDATE_COMMAND = "helm repo update";
    private static final String CORRECT_HELM_REPO_ADD_COMMAND = "helm repo add " + CHART_REPO_NAME + " " + CHART_REPO_URL;

    @Test
    public void shouldConstructInstallCommandUsingLocalChartArchiveWithNoArgumentsWithDisabledTls() {
        assertThat(
                HelmInstallCommand.commandWithArchive(
                        HELM_VERSION_2,
                        NAMESPACE,
                        RELEASE_NAME,
                        null,
                        CHART_ARCHIVE_NAME,
                        false).asString(),
                equalTo(CORRECT_HELM_INSTALL_COMMAND_USING_LOCAL_CHART_ARCHIVE)
        );
    }

    @Test
    public void shouldConstructInstallCommandUsingLocalChartArchiveWithNoArgumentsWithEnabledTls() {
        assertThat(
                HelmInstallCommand.commandWithArchive(
                        HELM_VERSION_2,
                        NAMESPACE,
                        RELEASE_NAME,
                        null,
                        CHART_ARCHIVE_NAME,
                        true).asString(),
                equalTo(CORRECT_HELM_INSTALL_COMMAND_USING_LOCAL_CHART_ARCHIVE + TLS)
        );
    }

    @Test
    public void shouldConstructInstallCommandUsingChartFromRepoWithNoArgumentsWithDisabledTls() {
        assertThat(
                HelmInstallCommand.commandWithRepo(
                        HELM_VERSION_2,
                        NAMESPACE,
                        RELEASE_NAME,
                        null,
                        CHART_NAME_WITH_REPO,
                        null, false).asString(),
                equalTo(CORRECT_HELM_INSTALL_COMMAND_USING_CHART_FROM_REPO));
        assertThat(
                HelmInstallCommand.commandWithRepo(
                        HELM_VERSION_2,
                        NAMESPACE,
                        RELEASE_NAME,
                        null,
                        CHART_NAME_WITH_REPO,
                        CHART_VERSION, false).asString(),
                equalTo(CORRECT_HELM_INSTALL_COMMAND_USING_CHART_FROM_REPO_WITH_VERSION));
    }

    @Test
    public void shouldConstructInstallCommandForV3UsingChartFromRepoWithNoArgumentsWithDisabledTls() {
        assertThat(
                HelmInstallCommand.commandWithRepo(
                        HelmCommand.HELM_VERSION_3,
                        NAMESPACE,
                        RELEASE_NAME,
                        null,
                        CHART_NAME_WITH_REPO,
                        null, false).asString(),
                equalTo(CORRECT_HELM_INSTALL_COMMAND_FOR_v3_USING_CHART_FROM_REPO));
        assertThat(
                HelmInstallCommand.commandWithRepo(
                        HelmCommand.HELM_VERSION_3,
                        NAMESPACE,
                        RELEASE_NAME,
                        null,
                        CHART_NAME_WITH_REPO,
                        CHART_VERSION, false).asString(),
                equalTo(CORRECT_HELM_INSTALL_COMMAND_FOR_V3_USING_CHART_FROM_REPO_WITH_VERSION));
    }

    @Test
    public void shouldConstructInstallCommandUsingChartFromRepoWithNoArgumentsWithEnabledTls() {
        assertThat(
                HelmInstallCommand.commandWithRepo(
                        HELM_VERSION_2,
                        NAMESPACE,
                        RELEASE_NAME,
                        null,
                        CHART_NAME_WITH_REPO,
                        null, true).asString(),
                equalTo(CORRECT_HELM_INSTALL_COMMAND_USING_CHART_FROM_REPO + TLS));
        assertThat(
                HelmInstallCommand.commandWithRepo(
                        HELM_VERSION_2,
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
        assertThat(
                HelmInstallCommand.commandWithArchive(
                        HELM_VERSION_2,
                        NAMESPACE,
                        RELEASE_NAME,
                        arguments,
                        CHART_ARCHIVE_NAME,
                        false).asString(),
                allOf(containsString(CORRECT_HELM_INSTALL_COMMAND_FIRST_PART),
                        containsString("testPersistenceName"),
                        containsString("testStorageClass"))
        );
    }

    @Test
    public void shouldConstructInstallCommandWithArgumentsWithEnabledTls() {
        Map<String, String> arguments = new HashMap<>();
        arguments.put("persistence.name", "testPersistenceName");
        arguments.put("persistence.storageClass", "testStorageClass");
        assertThat(
                HelmInstallCommand.commandWithArchive(
                        HELM_VERSION_2,
                        NAMESPACE,
                        RELEASE_NAME,
                        arguments,
                        CHART_ARCHIVE_NAME,
                        true).asString(),
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
        assertThat(HelmStatusCommand.command(HELM_VERSION_2, NAMESPACE, RELEASE_NAME, false).asString(), equalTo(CORRECT_HELM_STATUS_COMMAND));
    }

    @Test
    public void shouldConstructStatusCommandWithEnabledTls() {
        assertThat(HelmStatusCommand.command(HELM_VERSION_2, NAMESPACE, RELEASE_NAME, true).asString(), equalTo(CORRECT_HELM_STATUS_COMMAND + TLS));
    }

    @Test
    public void shouldConstructStatusCommandForV3() {
        assertThat(HelmStatusCommand.command(HelmCommand.HELM_VERSION_3, NAMESPACE, RELEASE_NAME, false).asString(), equalTo(CORRECT_HELM_STATUS_COMMAND_FOR_V3));
    }

    @Test
    public void shouldConstructUpgradeCommandWithDisabledTls() {
        assertThat(HelmUpgradeCommand.commandWithArchive(HELM_VERSION_2, RELEASE_NAME, CHART_ARCHIVE_NAME, false).asString(),
                equalTo(CORRECT_HELM_UPGRADE_COMMAND));
    }

    @Test
    public void shouldConstructUpgradeCommandWithEnabledTls() {
        assertThat(HelmUpgradeCommand.commandWithArchive(HELM_VERSION_2, RELEASE_NAME, CHART_ARCHIVE_NAME, true).asString(),
                equalTo(CORRECT_HELM_UPGRADE_COMMAND + TLS));
    }

    @Test
    public void shouldConstructVersionCommandWithDisabledTls() {
        assertThat(HelmVersionCommand.command(HELM_VERSION_2, false).asString(), equalTo(CORRECT_HELM_VERSION_COMMAND));
    }

    @Test
    public void shouldConstructVersionCommandWithEnabledTls() {
        assertThat(HelmVersionCommand.command(HELM_VERSION_2, true).asString(), equalTo(CORRECT_HELM_VERSION_COMMAND + TLS));
    }

    @Test
    public void shouldConstructRepoUpdateCommand() {
        assertThat(HelmRepoUpdateCommand.command().asString(), equalTo(CORRECT_HELM_REPO_UPDATE_COMMAND));
    }

    @Test
    public void shouldConstructRepoAddCommand() {
        assertThat(HelmRepoAddCommand.command(CHART_REPO_NAME, CHART_REPO_URL).asString(), equalTo(CORRECT_HELM_REPO_ADD_COMMAND));
    }

}
