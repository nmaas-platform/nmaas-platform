package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KServiceLifecycleManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster.KNamespaceService;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.exceptions.KServiceManipulationException;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.ssh.CommandExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Profile("env_kubernetes")
public class HelmKServiceManager implements KServiceLifecycleManager {

    static final String HELM_INSTALL_OPTION_PERSISTENCE_NAME = "persistence.name";
    static final String HELM_INSTALL_OPTION_PERSISTENCE_STORAGE_CLASS = "persistence.storageClass";
    static final String HELM_INSTALL_OPTION_NMAAS_CONFIG_ACTION = "nmaas.config.action";
    static final String HELM_INSTALL_OPTION_NMAAS_CONFIG_ACTION_VALUE = "clone_or_pull";
    static final String HELM_INSTALL_OPTION_NMAAS_CONFIG_REPOURL = "nmaas.config.repourl";

    private KubernetesRepositoryManager repositoryManager;
    private KNamespaceService namespaceService;
    private HelmCommandExecutor helmCommandExecutor;

    private String kubernetesPersistenceStorageClass;

    @Autowired
    public HelmKServiceManager(KubernetesRepositoryManager repositoryManager, KNamespaceService namespaceService, HelmCommandExecutor helmCommandExecutor) {
        this.repositoryManager = repositoryManager;
        this.namespaceService = namespaceService;
        this.helmCommandExecutor = helmCommandExecutor;
    }

    @Override
    public void deployService(Identifier deploymentId) throws KServiceManipulationException, InvalidDeploymentIdException {
        try {
            installHelmChart(deploymentId, repositoryManager.loadService(deploymentId));
        } catch (CommandExecutionException cee) {
            throw new KServiceManipulationException("Helm command execution failed -> " + cee.getMessage());
        }
    }

    private void installHelmChart(Identifier deploymentId, KubernetesNmServiceInfo serviceInfo) throws CommandExecutionException {
        KubernetesTemplate template = serviceInfo.getKubernetesTemplate();
        Identifier clientId = serviceInfo.getClientId();
        String repoUrl = serviceInfo.getGitLabProject().getCloneUrl();
        Map<String, String> arguments = new HashMap<>();
        arguments.put(HELM_INSTALL_OPTION_PERSISTENCE_NAME, deploymentId.value());
        arguments.put(HELM_INSTALL_OPTION_PERSISTENCE_STORAGE_CLASS, kubernetesPersistenceStorageClass);
        arguments.put(HELM_INSTALL_OPTION_NMAAS_CONFIG_ACTION, HELM_INSTALL_OPTION_NMAAS_CONFIG_ACTION_VALUE);
        arguments.put(HELM_INSTALL_OPTION_NMAAS_CONFIG_REPOURL, repoUrl);
        helmCommandExecutor.executeHelmInstallCommand(
                namespaceService.namespace(clientId),
                deploymentId,
                template.getArchive(),
                arguments
        );
    }

    @Override
    public boolean checkServiceDeployed(Identifier deploymentId) throws KServiceManipulationException {
        try {
            HelmPackageStatus status = helmCommandExecutor.executeHelmStatusCommand(deploymentId);
            return status.equals(HelmPackageStatus.DEPLOYED);
        } catch (CommandExecutionException cee) {
            throw new KServiceManipulationException("Helm command execution failed -> " + cee.getMessage());
        }
    }

    @Override
    public void deleteService(Identifier deploymentId) throws KServiceManipulationException  {
        try {
            helmCommandExecutor.executeHelmDeleteCommand(deploymentId);
        } catch (CommandExecutionException cee) {
            throw new KServiceManipulationException("Helm command execution failed -> " + cee.getMessage());
        }
    }

    @Override
    public void upgradeService(Identifier deploymentId) throws KServiceManipulationException, InvalidDeploymentIdException {
        KubernetesNmServiceInfo serviceInfo = repositoryManager.loadService(deploymentId);
        KubernetesTemplate template = serviceInfo.getKubernetesTemplate();
        try {
            helmCommandExecutor.executeHelmUpgradeCommand(
                    deploymentId,
                    template.getArchive()
            );
        } catch (CommandExecutionException cee) {
            throw new KServiceManipulationException("Helm command execution failed -> " + cee.getMessage());
        }
    }

    @Value("${kubernetes.persistence.class}")
    public void setKubernetesPersistenceStorageClass(String kubernetesPersistenceStorageClass) {
        this.kubernetesPersistenceStorageClass = kubernetesPersistenceStorageClass;
    }

}
