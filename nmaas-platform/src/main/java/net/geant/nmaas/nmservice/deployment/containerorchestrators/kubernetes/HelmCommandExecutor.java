package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.utils.ssh.CommandExecutionException;
import net.geant.nmaas.utils.ssh.SingleCommandExecutor;
import net.geant.nmaas.utils.ssh.SshConnectionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class HelmCommandExecutor {

    private String hostAddress;
    private String hostSshUsername;

    private boolean useLocalArchives;
    private String hostChartsDirectory;
    private String kubernetesNamespace;

    void executeHelmInstallCommand(Identifier deploymentId, String chartArchiveName, Map<String, String> arguments) throws CommandExecutionException {
        try {
            if (!useLocalArchives)
                throw new CommandExecutionException("Currently only referencing local chart archive is supported");
            String completeChartArchivePath = constructChartArchivePath(chartArchiveName);
            HelmInstallCommand command = HelmInstallCommand.command(
                    kubernetesNamespace,
                    deploymentId.value(),
                    arguments,
                    completeChartArchivePath
            );
            SingleCommandExecutor.getExecutor(hostAddress, hostSshUsername).executeSingleCommand(command);
        } catch (SshConnectionException
                | CommandExecutionException e) {
            throw new CommandExecutionException("Failed to execute helm install command -> " + e.getMessage());
        }
    }

    private String constructChartArchivePath(String chartArchiveName) {
        return baseChartArchivePath() + chartArchiveName;
    }

    private String baseChartArchivePath() {
        if (!hostChartsDirectory.endsWith("/"))
            return hostChartsDirectory.concat("/");
        return hostChartsDirectory;
    }

    @Value("${kubernetes.helm.host}")
    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    @Value("${kubernetes.helm.ssh.username}")
    public void setHostSshUsername(String hostSshUsername) {
        this.hostSshUsername = hostSshUsername;
    }

    @Value("${kubernetes.helm.charts.use.local.archives}")
    public void setUseLocalArchives(boolean useLocalArchives) {
        this.useLocalArchives = useLocalArchives;
    }

    @Value("${kubernetes.helm.charts.directory}")
    public void setHostChartsDirectory(String hostChartsDirectory) {
        this.hostChartsDirectory = hostChartsDirectory;
    }

    @Value("${kubernetes.namespace}")
    public void setKubernetesNamespace(String kubernetesNamespace) {
        this.kubernetesNamespace = kubernetesNamespace;
    }

}
