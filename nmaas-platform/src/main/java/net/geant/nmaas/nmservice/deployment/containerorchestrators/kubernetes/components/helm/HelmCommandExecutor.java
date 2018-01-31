package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import net.geant.nmaas.externalservices.inventory.kubernetes.KubernetesClusterManager;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.utils.ssh.CommandExecutionException;
import net.geant.nmaas.utils.ssh.SingleCommandExecutor;
import net.geant.nmaas.utils.ssh.SshConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class HelmCommandExecutor {

    @Autowired
    private KubernetesClusterManager cluster;

    private boolean useLocalArchives;
    private String defaultKubernetesNamespace;

    public void executeHelmInstallCommand(String releaseName, String chartArchiveName, Map<String, String> arguments) throws CommandExecutionException {
        executeHelmInstallCommand(defaultKubernetesNamespace, releaseName, chartArchiveName, arguments);
    }

    public void executeHelmInstallCommand(Identifier deploymentId, String chartArchiveName, Map<String, String> arguments) throws CommandExecutionException {
        executeHelmInstallCommand(defaultKubernetesNamespace, deploymentId.value(), chartArchiveName, arguments);
    }

    private void executeHelmInstallCommand(String namespace, String releaseName, String chartArchiveName, Map<String, String> arguments)
            throws CommandExecutionException {
        try {
            if (!useLocalArchives)
                throw new CommandExecutionException("Currently only referencing local chart archive is supported");
            String completeChartArchivePath = constructChartArchivePath(chartArchiveName);
            HelmInstallCommand command = HelmInstallCommand.command(
                    namespace,
                    releaseName,
                    arguments,
                    completeChartArchivePath
            );
            singleCommandExecutor().executeSingleCommand(command);
        } catch (SshConnectionException
                | CommandExecutionException e) {
            throw new CommandExecutionException("Failed to execute helm install command -> " + e.getMessage());
        }
    }

    private String constructChartArchivePath(String chartArchiveName) {
        return baseChartArchivePath() + chartArchiveName;
    }

    private String baseChartArchivePath() {
        String hostChartsDirectory = cluster.getHelmHostChartsDirectory();
        if (!hostChartsDirectory.endsWith("/"))
            return hostChartsDirectory.concat("/");
        return hostChartsDirectory;
    }

    public void executeHelmDeleteCommand(Identifier deploymentId) throws CommandExecutionException {
        try {
            HelmDeleteCommand command = HelmDeleteCommand.command(deploymentId.value());
            singleCommandExecutor().executeSingleCommand(command);
        } catch (SshConnectionException
                | CommandExecutionException e) {
            throw new CommandExecutionException("Failed to execute helm delete command -> " + e.getMessage());
        }
    }

    public HelmPackageStatus executeHelmStatusCommand(Identifier deploymentId) throws CommandExecutionException {
        return executeHelmStatusCommand(deploymentId.value());
    }

    HelmPackageStatus executeHelmStatusCommand(String releaseName) throws CommandExecutionException {
        try {
            HelmStatusCommand command = HelmStatusCommand.command(releaseName);
            String output = singleCommandExecutor().executeSingleCommandAndReturnOutput(command);
            return parseStatus(output);
        } catch (SshConnectionException
                | CommandExecutionException e) {
            throw new CommandExecutionException("Failed to execute helm status command -> " + e.getMessage());
        }
    }

    HelmPackageStatus parseStatus(String output) {
        if(output.contains("STATUS: DEPLOYED"))
            return HelmPackageStatus.DEPLOYED;
        else
            return HelmPackageStatus.UNKNOWN;
    }

    public List<String> executeHelmListCommand() throws CommandExecutionException {
        try {
            HelmListCommand command = HelmListCommand.command();
            String output = singleCommandExecutor().executeSingleCommandAndReturnOutput(command);
            return Arrays.asList(output.split("\n"));
        } catch (SshConnectionException
                | CommandExecutionException e) {
            throw new CommandExecutionException("Failed to execute helm list command -> " + e.getMessage());
        }
    }

    private SingleCommandExecutor singleCommandExecutor() {
        return SingleCommandExecutor.getExecutor(cluster.getHelmHostAddress(), cluster.getHelmHostSshUsername());
    }

    @Value("${kubernetes.helm.charts.use.local.archives}")
    public void setUseLocalArchives(boolean useLocalArchives) {
        this.useLocalArchives = useLocalArchives;
    }

    @Value("${kubernetes.namespace}")
    public void setDefaultKubernetesNamespace(String defaultKubernetesNamespace) {
        this.defaultKubernetesNamespace = defaultKubernetesNamespace;
    }

}
