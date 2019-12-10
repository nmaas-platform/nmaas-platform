package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.utils.ssh.CommandExecutionException;
import net.geant.nmaas.utils.ssh.SingleCommandExecutor;
import net.geant.nmaas.utils.ssh.SshConnectionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class HelmCommandExecutor {

    @Value("${helm.address}")
    String helmAddress;

    @Value("${helm.username}")
    String helmUsername;

    @Value("${helm.useLocalCharts}")
    Boolean useLocalCharts;

    @Value("${helm.repositoryName}")
    String helmRepositoryName;

    @Value("${helm.chartsDirectory}")
    String helmChartsDirectory;

    @Value("${helm.enableTls}")
    Boolean enableTls;

    public void executeHelmInstallCommand(String kubernetesNamespace, String releaseName, KubernetesTemplate template, Map<String, String> arguments) {
        executeInstall(kubernetesNamespace, releaseName, template, arguments);
    }

    private void executeInstall(String namespace, String releaseName, KubernetesTemplate template, Map<String, String> arguments) {
        try {
            HelmInstallCommand command;
            if (useLocalCharts) {
                command = HelmInstallCommand.commandWithArchive(
                        namespace,
                        releaseName,
                        arguments,
                        constructChartArchivePath(template.getArchive()),
                        enableTls
                );
            } else {
                command = HelmInstallCommand.commandWithRepo(
                        namespace,
                        releaseName,
                        arguments,
                        constructChartNameWithRepo(template.getChart().getName()),
                        template.getChart().getVersion(),
                        enableTls
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
        String hostChartsDirectory = helmChartsDirectory;
        if (!hostChartsDirectory.endsWith("/"))
            return hostChartsDirectory.concat("/");
        return hostChartsDirectory;
    }

    private String constructChartNameWithRepo(String chartName) {
        return helmRepositoryName + "/" + chartName;
    }

    void executeHelmDeleteCommand(String releaseName) {
        try {
            HelmDeleteCommand command = HelmDeleteCommand.command(releaseName, enableTls);
            singleCommandExecutor().executeSingleCommand(command);
        } catch (SshConnectionException
                | CommandExecutionException e) {
            throw new CommandExecutionException("Failed to execute helm delete command -> " + e.getMessage());
        }
    }

    HelmPackageStatus executeHelmStatusCommand(String releaseName) {
        return executeStatus(releaseName);
    }

    private HelmPackageStatus executeStatus(String releaseName) {
        try {
            HelmStatusCommand command = HelmStatusCommand.command(releaseName, enableTls);
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

    public List<String> executeHelmListCommand() {
        try {
            HelmListCommand command = HelmListCommand.command(enableTls);
            String output = singleCommandExecutor().executeSingleCommandAndReturnOutput(command);
            return Arrays.asList(output.split("\n"));
        } catch (SshConnectionException
                | CommandExecutionException e) {
            throw new CommandExecutionException("Failed to execute helm list command -> " + e.getMessage());
        }
    }

    void executeHelmUpgradeCommand(String releaseName, String chartArchiveName) {
        try {
            HelmUpgradeCommand command;
            if (useLocalCharts) {
                command = HelmUpgradeCommand.commandWithArchive(
                        releaseName,
                        constructChartArchivePath(chartArchiveName),
                        enableTls
                );
            } else {
                throw new CommandExecutionException("Currently only referencing local chart archive is supported");
            }
            singleCommandExecutor().executeSingleCommand(command);
        } catch (SshConnectionException e) {
            throw new CommandExecutionException("Failed to execute helm upgrade command -> " + e.getMessage());
        }
    }

    void executeVersionCommand() {
        try{
            singleCommandExecutor().executeSingleCommand(HelmVersionCommand.command(enableTls));
        } catch(SshConnectionException e) {
            throw new CommandExecutionException("Failed to execute helm version command -> " + e.getMessage());
        }
    }

    private SingleCommandExecutor singleCommandExecutor() {
        return SingleCommandExecutor.getExecutor(helmAddress, helmUsername);
    }

    void executeHelmRepoUpdateCommand() {
        try{
            singleCommandExecutor().executeSingleCommand(HelmRepoUpdateCommand.command());
        } catch(SshConnectionException e) {
            throw new CommandExecutionException("Failed to execute helm repository update command -> " + e.getMessage());
        }
    }
}
