package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import lombok.AllArgsConstructor;
import net.geant.nmaas.externalservices.inventory.kubernetes.KClusterDeploymentManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.KClusterIngressManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.KNamespaceService;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.IngressCertificateConfigOption;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.IngressResourceConfigOption;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KServiceLifecycleManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.exceptions.KServiceManipulationException;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import net.geant.nmaas.utils.ssh.CommandExecutionException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@AllArgsConstructor
@Profile("env_kubernetes")
public class HelmKServiceManager implements KServiceLifecycleManager {

    static final String HELM_INSTALL_OPTION_PERSISTENCE_NAME = "persistence.name";
    static final String HELM_INSTALL_OPTION_PERSISTENCE_STORAGE_CLASS = "persistence.storageClass";
    static final String HELM_INSTALL_OPTION_PERSISTENCE_STORAGE_SPACE = "persistence.size";
    static final String HELM_INSTALL_OPTION_INGRESS_ENABLED = "ingress.enabled";
    static final String HELM_INSTALL_OPTION_DEDICATED_WORKERS = "domain";
    static final String HELM_COMMAND_EXECUTION_FAILED_ERROR_MESSAGE = "Helm command execution failed -> ";

    private KubernetesRepositoryManager repositoryManager;
    private KNamespaceService namespaceService;
    private KClusterDeploymentManager deploymentManager;
    private KClusterIngressManager ingressManager;
    private HelmCommandExecutor helmCommandExecutor;

    @Override
    @Loggable(LogLevel.DEBUG)
    public void deployService(Identifier deploymentId) {
        try {
            installHelmChart(deploymentId, repositoryManager.loadService(deploymentId));
        } catch (CommandExecutionException cee) {
            throw new KServiceManipulationException(HELM_COMMAND_EXECUTION_FAILED_ERROR_MESSAGE + cee.getMessage());
        }
    }

    private void installHelmChart(Identifier deploymentId, KubernetesNmServiceInfo serviceInfo) {
        KubernetesTemplate template = serviceInfo.getKubernetesTemplate();
        String domain = serviceInfo.getDomain();
        String serviceExternalURL = serviceInfo.getServiceExternalUrl();
        Map<String, String> arguments = new HashMap<>();
        arguments.put(HELM_INSTALL_OPTION_PERSISTENCE_NAME, deploymentId.value());
        Optional<String> storageClass = deploymentManager.getStorageClass(domain);
        storageClass.ifPresent(s -> arguments.put(HELM_INSTALL_OPTION_PERSISTENCE_STORAGE_CLASS, s));
        if(deploymentManager.getForceDedicatedWorkers()){
            arguments.put(HELM_INSTALL_OPTION_DEDICATED_WORKERS, domain);
        }
        arguments.put(HELM_INSTALL_OPTION_PERSISTENCE_STORAGE_SPACE, getStorageSpaceString(serviceInfo.getStorageSpace()));
        arguments.put(HELM_INSTALL_OPTION_INGRESS_ENABLED,
                String.valueOf(IngressResourceConfigOption.DEPLOY_FROM_CHART.equals(ingressManager.getResourceConfigOption())));
        if (IngressResourceConfigOption.DEPLOY_FROM_CHART.equals(ingressManager.getResourceConfigOption())) {
            arguments.putAll(HelmChartVariables.ingressVariablesMap(
                    serviceExternalURL,
                    ingressManager.getSupportedIngressClass(),
                    ingressManager.getTlsSupported())
            );
            if(ingressManager.getTlsSupported()) {
                arguments.putAll(HelmChartVariables.ingressVariablesAddTls(
                        ingressManager.getIssuerOrWildcardName(),
                        ingressManager.getCertificateConfigOption().equals(IngressCertificateConfigOption.USE_LETSENCRYPT)
                ));
            }
        }
        if(serviceInfo.getAdditionalParameters() != null && !serviceInfo.getAdditionalParameters().isEmpty()){
            serviceInfo.getAdditionalParameters().forEach(arguments::put);
        }
        helmCommandExecutor.executeHelmInstallCommand(
                namespaceService.namespace(domain),
                deploymentId,
                template,
                arguments
        );
    }

    private String getStorageSpaceString(Integer storageSpace){
        return storageSpace.toString() + "Gi";
    }

    @Override
    @Loggable(LogLevel.DEBUG)
    public boolean checkServiceDeployed(Identifier deploymentId) {
        try {
            HelmPackageStatus status = helmCommandExecutor.executeHelmStatusCommand(deploymentId);
            return status.equals(HelmPackageStatus.DEPLOYED);
        } catch (CommandExecutionException cee) {
            throw new KServiceManipulationException(HELM_COMMAND_EXECUTION_FAILED_ERROR_MESSAGE + cee.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.DEBUG)
    public void deleteService(Identifier deploymentId) {
        try {
            helmCommandExecutor.executeHelmDeleteCommand(deploymentId);
        } catch (CommandExecutionException cee) {
            throw new KServiceManipulationException(HELM_COMMAND_EXECUTION_FAILED_ERROR_MESSAGE + cee.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.DEBUG)
    public void upgradeService(Identifier deploymentId) {
        KubernetesNmServiceInfo serviceInfo = repositoryManager.loadService(deploymentId);
        KubernetesTemplate template = serviceInfo.getKubernetesTemplate();
        try {
            helmCommandExecutor.executeHelmUpgradeCommand(
                    deploymentId,
                    template.getArchive()
            );
        } catch (CommandExecutionException cee) {
            throw new KServiceManipulationException(HELM_COMMAND_EXECUTION_FAILED_ERROR_MESSAGE + cee.getMessage());
        }
    }

}
