package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.commands.HelmDeleteCommand;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.commands.HelmInstallCommand;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.commands.HelmListCommand;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.commands.HelmRepoAddCommand;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.commands.HelmRepoUpdateCommand;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.commands.HelmStatusCommand;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.commands.HelmUninstallCommand;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.commands.HelmUpgradeCommand;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.commands.HelmVersionCommand;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.utils.bash.CommandExecutionException;
import net.geant.nmaas.utils.bash.CommandExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmCommand.HELM_VERSION_2;

@NoArgsConstructor
@Component
public class HelmCommandExecutor {

    private CommandExecutor commandExecutor;
    @Setter
    private String helmVersion;
    @Setter
    private String helmRepositoryName;
    private Boolean enableTls;

    @Autowired
    public HelmCommandExecutor(CommandExecutor commandExecutor,
                               @Value("${helm.version:v3}") String helmVersion,
                               @Value("${helm.repositoryName}") String helmRepositoryName,
                               @Value("${helm.enableTls:false}") Boolean enableTls) {
        this.commandExecutor = commandExecutor;
        this.helmVersion = helmVersion;
        this.helmRepositoryName = helmRepositoryName;
        this.enableTls = enableTls;
    }

    void executeHelmInstallCommand(String namespace, String releaseName, KubernetesTemplate template, Map<String, String> arguments) {
        executeInstall(namespace, releaseName, template, arguments);
    }

    private void executeInstall(String namespace, String releaseName, KubernetesTemplate template, Map<String, String> arguments) {
        try {
            HelmInstallCommand command = HelmInstallCommand.commandWithRepo(
                    helmVersion,
                    namespace,
                    releaseName,
                    arguments,
                    constructChartNameWithRepo(template.getChart().getName()),
                    template.getChart().getVersion(),
                    enableTls
            );
            commandExecutor.execute(command);
        } catch (CommandExecutionException e) {
            throw new CommandExecutionException("Failed to execute helm install command -> " + e.getMessage());
        }
    }

    String constructChartNameWithRepo(String chartName) {
        return chartName.contains("/") ? chartName : helmRepositoryName + "/" + chartName;
    }

    void executeHelmDeleteCommand(String namespace, String releaseName) {
        try {
            HelmCommand command;
            if (HELM_VERSION_2.equals(helmVersion)) {
                command = HelmDeleteCommand.command(releaseName, enableTls);
            } else if (HelmCommand.HELM_VERSION_3.equals(helmVersion)) {
                command = HelmUninstallCommand.command(namespace, releaseName);
            } else {
                throw new CommandExecutionException("Unknown Helm version in use: " + helmVersion);
            }
            commandExecutor.execute(command);
        } catch (CommandExecutionException e) {
            throw new CommandExecutionException("Failed to execute helm delete command -> " + e.getMessage());
        }
    }

    HelmPackageStatus executeHelmStatusCommand(String namespace, String releaseName) {
        return executeStatus(namespace, releaseName);
    }

    private HelmPackageStatus executeStatus(String namespace, String releaseName) {
        try {
            HelmStatusCommand command = HelmStatusCommand.command(
                    helmVersion,
                    namespace,
                    releaseName,
                    enableTls
            );
            String output = commandExecutor.executeWithOutput(command);
            return parseStatus(output);
        } catch (CommandExecutionException e) {
            throw new CommandExecutionException("Failed to execute helm status command -> " + e.getMessage());
        }
    }

    HelmPackageStatus parseStatus(String output) {
        if (HELM_VERSION_2.equals(helmVersion) && output.contains("STATUS: DEPLOYED")) {
            return HelmPackageStatus.DEPLOYED;
        } else if (HelmCommand.HELM_VERSION_3.equals(helmVersion) && output.contains("STATUS: deployed")) {
            return HelmPackageStatus.DEPLOYED;
        } else {
            return HelmPackageStatus.UNKNOWN;
        }
    }

    public List<String> executeHelmListCommand(String namespace) {
        try {
            HelmListCommand command = HelmListCommand.command(
                    helmVersion,
                    namespace,
                    enableTls
            );
            String output = commandExecutor.executeWithOutput(command);
            return Arrays.asList(output.split("\n"));
        } catch (CommandExecutionException e) {
            throw new CommandExecutionException("Failed to execute helm list command -> " + e.getMessage());
        }
    }

    void executeHelmUpgradeCommand(String namespace, String releaseName, KubernetesTemplate template) {
        try {
            HelmUpgradeCommand command = HelmUpgradeCommand.commandWithRepo(
                    helmVersion,
                    namespace,
                    releaseName,
                    constructChartNameWithRepo(template.getChart().getName()),
                    template.getChart().getVersion(),
                    enableTls
            );
            commandExecutor.execute(command);
        } catch (CommandExecutionException e) {
            throw new CommandExecutionException("Failed to execute helm upgrade command -> " + e.getMessage());
        }
    }

    void executeVersionCommand() {
        try {
            commandExecutor.execute(HelmVersionCommand.command(helmVersion, enableTls));
        } catch (CommandExecutionException e) {
            throw new CommandExecutionException("Failed to execute helm version command -> " + e.getMessage());
        }
    }

    void executeHelmRepoUpdateCommand() {
        try {
            commandExecutor.execute(HelmRepoUpdateCommand.command());
        } catch (CommandExecutionException e) {
            throw new CommandExecutionException("Failed to execute helm repository update command -> " + e.getMessage());
        }
    }

    void executeHelmRepoAddCommand(String repoName, String repoUrl) {
        try {
            commandExecutor.execute(HelmRepoAddCommand.command(repoName, repoUrl));
        } catch (CommandExecutionException e) {
            throw new CommandExecutionException("Failed to execute helm repository add command -> " + e.getMessage());
        }
    }

}
