package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import net.geant.nmaas.externalservices.inventory.kubernetes.KClusterHelmManager;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.utils.ssh.CommandExecutionException;
import net.geant.nmaas.utils.ssh.SingleCommandExecutor;
import net.geant.nmaas.utils.ssh.SshConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class HelmCommandExecutor {

    private KClusterHelmManager clusterHelmManager;

    @Autowired
    public HelmCommandExecutor(KClusterHelmManager clusterHelmManager) {
        this.clusterHelmManager = clusterHelmManager;
    }

    public void executeHelmInstallCommand(String kubernetesNamespace, String releaseName, String chartArchiveName, Map<String, String> arguments) throws CommandExecutionException {
        executeInstall(kubernetesNamespace, releaseName, chartArchiveName, arguments);
    }

    public void executeHelmInstallCommand(String kubernetesNamespace, Identifier deploymentId, String chartArchiveName, Map<String, String> arguments) throws CommandExecutionException {
        executeInstall(kubernetesNamespace, deploymentId.value(), chartArchiveName, arguments);
    }

    private void executeInstall(String namespace, String releaseName, String chartArchiveName, Map<String, String> arguments)
            throws CommandExecutionException {
        try {
            if (!clusterHelmManager.getUseLocalChartArchives())
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
        String hostChartsDirectory = clusterHelmManager.getHelmHostChartsDirectory();
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

    public void executeHelmUpgradeCommand(Identifier deploymentId, String chartArchiveName)
            throws CommandExecutionException {
        if (!clusterHelmManager.getUseLocalChartArchives())
            throw new CommandExecutionException("Currently only referencing local chart archive is supported");
        try {
            String completeChartArchivePath = constructChartArchivePath(chartArchiveName);
            HelmUpgradeCommand command = HelmUpgradeCommand.command(
                    deploymentId.value(),
                    completeChartArchivePath
            );
            singleCommandExecutor().executeSingleCommand(command);
        } catch (SshConnectionException e) {
            throw new CommandExecutionException("Failed to execute helm upgrade command -> " + e.getMessage());
        }
    }

    private SingleCommandExecutor singleCommandExecutor() {
        return SingleCommandExecutor.getExecutor(clusterHelmManager.getHelmHostAddress(), clusterHelmManager.getHelmHostSshUsername());
    }

}
