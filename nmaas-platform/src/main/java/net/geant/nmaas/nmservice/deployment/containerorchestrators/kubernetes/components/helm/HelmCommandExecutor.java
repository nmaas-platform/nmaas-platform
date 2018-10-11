package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import net.geant.nmaas.externalservices.inventory.kubernetes.KClusterHelmManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.utils.ssh.CommandExecutionException;
import net.geant.nmaas.utils.ssh.SingleCommandExecutor;
import net.geant.nmaas.utils.ssh.SshConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class HelmCommandExecutor {

    private KClusterHelmManager clusterHelmManager;

    @Autowired
    HelmCommandExecutor(KClusterHelmManager clusterHelmManager) {
        this.clusterHelmManager = clusterHelmManager;
    }

    public void executeHelmInstallCommand(String kubernetesNamespace, String releaseName, KubernetesTemplate template, Map<String, String> arguments) throws CommandExecutionException {
        executeInstall(kubernetesNamespace, releaseName, template, arguments);
    }

    void executeHelmInstallCommand(String kubernetesNamespace, Identifier deploymentId, KubernetesTemplate template, Map<String, String> arguments) throws CommandExecutionException {
        executeInstall(kubernetesNamespace, deploymentId.value(), template, arguments);
    }

    private void executeInstall(String namespace, String releaseName, KubernetesTemplate template, Map<String, String> arguments)
            throws CommandExecutionException {
        try {
            HelmInstallCommand command;
            if (clusterHelmManager.getUseLocalChartArchives()) {
                command = HelmInstallCommand.commandWithArchive(
                        namespace,
                        releaseName,
                        arguments,
                        constructChartArchivePath(template.getArchive())
                );
            } else {
                command = HelmInstallCommand.commandWithRepo(
                        namespace,
                        releaseName,
                        arguments,
                        constructChartNameWithRepo(template.getChart().getName()),
                        template.getChart().getVersion()
                );
            }
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

    private String constructChartNameWithRepo(String chartName) {
        return clusterHelmManager.getHelmChartRepositoryName() + "/" + chartName;
    }

    void executeHelmDeleteCommand(Identifier deploymentId) throws CommandExecutionException {
        try {
            HelmDeleteCommand command = HelmDeleteCommand.command(deploymentId.value());
            singleCommandExecutor().executeSingleCommand(command);
        } catch (SshConnectionException
                | CommandExecutionException e) {
            throw new CommandExecutionException("Failed to execute helm delete command -> " + e.getMessage());
        }
    }

    HelmPackageStatus executeHelmStatusCommand(Identifier deploymentId) throws CommandExecutionException {
        return executeHelmStatusCommand(deploymentId.value());
    }

    private HelmPackageStatus executeHelmStatusCommand(String releaseName) throws CommandExecutionException {
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

    void executeHelmUpgradeCommand(Identifier deploymentId, String chartArchiveName)
            throws CommandExecutionException {
        try {
            HelmUpgradeCommand command;
            if (clusterHelmManager.getUseLocalChartArchives()) {
                command = HelmUpgradeCommand.commandWithArchive(
                        deploymentId.value(),
                        constructChartArchivePath(chartArchiveName)
                );
            } else {
                throw new CommandExecutionException("Currently only referencing local chart archive is supported");
            }
            singleCommandExecutor().executeSingleCommand(command);
        } catch (SshConnectionException e) {
            throw new CommandExecutionException("Failed to execute helm upgrade command -> " + e.getMessage());
        }
    }

    void executeVersionCommand() throws CommandExecutionException{
        try{
            singleCommandExecutor().executeSingleCommand(HelmVersionCommand.command());
        } catch(SshConnectionException e) {
            throw new CommandExecutionException("Failed to execute helm version command -> " + e.getMessage());
        }
    }

    private SingleCommandExecutor singleCommandExecutor() {
        return SingleCommandExecutor.getExecutor(clusterHelmManager.getHelmHostAddress(), clusterHelmManager.getHelmHostSshUsername());
    }

}
