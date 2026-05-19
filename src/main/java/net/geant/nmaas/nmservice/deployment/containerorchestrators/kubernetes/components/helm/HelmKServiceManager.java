package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.kubernetes.KubernetesClusterDeploymentManager;
import net.geant.nmaas.kubernetes.KubernetesClusterIngressManager;
import net.geant.nmaas.kubernetes.KubernetesClusterNamespaceService;
import net.geant.nmaas.kubernetes.remote.entities.IngressCertificateConfigOption;
import net.geant.nmaas.kubernetes.remote.entities.IngressResourceConfigOption;
import net.geant.nmaas.kubernetes.remote.entities.KCluster;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KServiceLifecycleManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethod;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceStorageVolume;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.exceptions.KServiceManipulationException;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.repositories.DomainTechDetailsRepository;
import net.geant.nmaas.utils.bash.CommandExecutionException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static net.geant.nmaas.kubernetes.remote.entities.IngressResourceConfigOption.DEPLOY_FROM_CHART;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesManager.PUBLIC_ACCESS_SELECTOR_ARGUMENT_EXPRESSION_PREFIX;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesManager.RANDOM_ARGUMENT_EXPRESSION_PREFIX;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType.DEFAULT;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType.EXTERNAL;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType.PUBLIC;

@Component
@RequiredArgsConstructor
@Slf4j
public class HelmKServiceManager implements KServiceLifecycleManager {

    static final String HELM_INSTALL_OPTION_DEDICATED_WORKERS = "domain";
    static final String HELM_COMMAND_EXECUTION_FAILED_ERROR_MESSAGE = "Helm command execution failed -> ";

    private final KubernetesRepositoryManager repositoryManager;
    private final KubernetesClusterNamespaceService namespaceService;
    private final KubernetesClusterDeploymentManager deploymentManager;
    private final KubernetesClusterIngressManager ingressManager;
    private final HelmCommandExecutor helmCommandExecutor;
    private final DomainTechDetailsRepository domainTechDetailsRepository;

    @Setter
    @Value("${helm.update.async.enabled}")
    private boolean helmRepoUpdateAsyncEnabled;

    @Override
    @Loggable(LogLevel.TRACE)
    public void deployService(Identifier deploymentId) {
        try {
            KubernetesNmServiceInfo serviceInfo = repositoryManager.loadService(deploymentId);
            if (!helmRepoUpdateAsyncEnabled && !HelmChartUtils.isOciChart(serviceInfo.getKubernetesTemplate())) {
                updateHelmRepo();
            }
            installHelmChart(serviceInfo);
        } catch (CommandExecutionException cee) {
            throw new KServiceManipulationException(HELM_COMMAND_EXECUTION_FAILED_ERROR_MESSAGE + cee.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.DEBUG)
    public void updateHelmRepo() {
        helmCommandExecutor.executeHelmRepoUpdateCommand();
    }

    private void installHelmChart(KubernetesNmServiceInfo serviceInfo) {
        helmCommandExecutor.executeHelmInstallCommand(
                getTargetNamespace(serviceInfo),
                serviceInfo.getDescriptiveDeploymentId().getValue(),
                serviceInfo.getKubernetesTemplate(),
                createArgumentsMap(serviceInfo),
                serviceInfo.getRemoteCluster() != null ? serviceInfo.getRemoteCluster().getPathConfigFile() : null
        );
    }

    private String getTargetNamespace(KubernetesNmServiceInfo serviceInfo) {
        if (serviceInfo.getRemoteCluster() != null) {
            return namespaceService.namespace(serviceInfo.getRemoteCluster(), serviceInfo.getDomain());
        }
        return namespaceService.namespace(serviceInfo.getDomain());
    }

    private Map<String, String> createArgumentsMap(KubernetesNmServiceInfo serviceInfo) {
        Map<String, String> arguments = new HashMap<>();

        Set<ServiceStorageVolume> serviceStorageVolumes = serviceInfo.getStorageVolumes();

        if (serviceInfo.getRemoteCluster() == null) {
            if (deploymentManager.getForceDedicatedWorkers()) {
                arguments.put(HELM_INSTALL_OPTION_DEDICATED_WORKERS, serviceInfo.getDomain());
            }
            Set<ServiceAccessMethod> externalAccessMethods = serviceExternalAccessMethods(serviceInfo.getAccessMethods());
            if (!externalAccessMethods.isEmpty()) {
                arguments.putAll(getIngressVariables(ingressManager.getResourceConfigOption(), externalAccessMethods, serviceInfo.getDomain(), null));
            }
            if (!serviceStorageVolumes.isEmpty()) {
                arguments.putAll(getPersistenceVariables(serviceStorageVolumes, deploymentManager.getStorageClass(serviceInfo.getDomain()), serviceInfo.getDescriptiveDeploymentId().getValue()));
            }
            //case when deploy on remote cluster
        } else {
            if (!serviceStorageVolumes.isEmpty()) {
                arguments.putAll(getPersistenceVariables(serviceStorageVolumes, serviceInfo.getRemoteCluster().getDeployment().getStorageClass(), serviceInfo.getDescriptiveDeploymentId().getValue()));
            }
            if (serviceInfo.getRemoteCluster().getDeployment().getForceDedicatedWorkers()) {
                arguments.put(HELM_INSTALL_OPTION_DEDICATED_WORKERS, serviceInfo.getDomain());
            }
            Set<ServiceAccessMethod> externalAccessMethods = serviceExternalAccessMethods(serviceInfo.getAccessMethods());
            if (!externalAccessMethods.isEmpty()) {
                arguments.putAll(getIngressVariables(serviceInfo.getRemoteCluster().getIngress().getResourceConfigOption(), externalAccessMethods, serviceInfo.getDomain(), serviceInfo.getRemoteCluster()));
            }
        }
        if (serviceInfo.getAdditionalParameters() != null && !serviceInfo.getAdditionalParameters().isEmpty()) {
            arguments.putAll(removeRedundantParameters(serviceInfo.getAdditionalParameters()));
        }
        return arguments;
    }

    private static Map<String, String> getPersistenceVariables(Set<ServiceStorageVolume> serviceStorageVolumes, Optional<String> storageClass, String storageName) {
        return HelmChartVariables.persistenceVariablesMap(
                serviceStorageVolumes,
                storageClass,
                storageName);
    }

    static Set<ServiceAccessMethod> serviceExternalAccessMethods(Set<ServiceAccessMethod> accessMethods) {
        return accessMethods.stream()
                .filter(ServiceAccessMethod::isEnabled)
                .filter(m -> m.isOfType(DEFAULT) || m.isOfType(EXTERNAL) || m.isOfType(PUBLIC))
                .collect(Collectors.toSet());
    }

    static Map<String, String> removeRedundantParameters(Map<String, String> additionalParameters) {
        return additionalParameters.entrySet().stream().filter(entry ->
                !entry.getKey().contains(RANDOM_ARGUMENT_EXPRESSION_PREFIX)
                        && !entry.getKey().contains(PUBLIC_ACCESS_SELECTOR_ARGUMENT_EXPRESSION_PREFIX)
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, String> getIngressVariables(IngressResourceConfigOption ingressResourceConfigOption, Set<ServiceAccessMethod> externalAccessMethods, String domain, KCluster cluster) {
        return HelmChartVariables.ingressVariablesMap(
                DEPLOY_FROM_CHART.equals(ingressResourceConfigOption),
                externalAccessMethods,
                getIngressClass(domain, cluster),
                ingressManager.getPublicIngressClass(),
                ingressManager.getTlsSupported(),
                ingressManager.getIssuerOrWildcardName(),
                ingressManager.getCertificateConfigOption().equals(IngressCertificateConfigOption.USE_LETSENCRYPT)
        );
    }

    private String getIngressClass(String domain, KCluster cluster) {
        if (cluster != null) {
            return cluster.getIngress().getSupportedIngressClass();
        } else if (Boolean.TRUE.equals(ingressManager.getIngressPerDomain())) {
            return domainTechDetailsRepository.findByDomainCodename(domain).orElseThrow(() -> new IllegalArgumentException("DomainTechDetails cannot be found for domain " + domain)).getKubernetesIngressClass();
        }
        return ingressManager.getSupportedIngressClass();
    }

    @Override
    @Loggable(LogLevel.TRACE)
    public boolean checkServiceDeployed(Identifier deploymentId) {
        final KubernetesNmServiceInfo serviceInfo = repositoryManager.loadService(deploymentId);
        try {
            HelmPackageStatus status = helmCommandExecutor.executeHelmStatusCommand(
                    namespaceService.namespace(repositoryManager.loadDomain(deploymentId)),
                    repositoryManager.loadDescriptiveDeploymentId(deploymentId).getValue(),
                    serviceInfo.getRemoteCluster() != null ? serviceInfo.getRemoteCluster().getPathConfigFile() : null
            );
            return status.equals(HelmPackageStatus.DEPLOYED);
        } catch (CommandExecutionException cee) {
            throw new KServiceManipulationException(HELM_COMMAND_EXECUTION_FAILED_ERROR_MESSAGE + cee.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.TRACE)
    public void deleteServiceIfExists(Identifier deploymentId) {
        final String namespace = namespaceService.namespace(repositoryManager.loadDomain(deploymentId));
        final Identifier descriptiveDeploymentId = repositoryManager.loadDescriptiveDeploymentId(deploymentId);
        final KubernetesNmServiceInfo serviceInfo = repositoryManager.loadService(deploymentId);
        try {
            if (checkIfServiceExists(namespace, deploymentId, descriptiveDeploymentId)) {
                helmCommandExecutor.executeHelmDeleteCommand(
                        namespace,
                        descriptiveDeploymentId.getValue(),
                        serviceInfo.getRemoteCluster() != null ? serviceInfo.getRemoteCluster().getPathConfigFile() : null
                );
            }
        } catch (CommandExecutionException cee) {
            throw new KServiceManipulationException(HELM_COMMAND_EXECUTION_FAILED_ERROR_MESSAGE + cee.getMessage());
        }
    }

    private boolean checkIfServiceExists(String namespace, Identifier deploymentId, Identifier descriptiveDeploymentId) {
        final KubernetesNmServiceInfo serviceInfo = repositoryManager.loadService(deploymentId);
        return helmCommandExecutor.executeHelmListCommand(
                namespace,
                serviceInfo.getRemoteCluster() != null ? serviceInfo.getRemoteCluster().getPathConfigFile() : null
        ).contains(descriptiveDeploymentId.value());
    }

    @Override
    @Loggable(LogLevel.TRACE)
    public void upgradeService(Identifier deploymentId, KubernetesTemplate targetVersion) {
        try {
            final KubernetesNmServiceInfo serviceInfo = repositoryManager.loadService(deploymentId);
            if (!helmRepoUpdateAsyncEnabled && !HelmChartUtils.isOciChart(targetVersion)) {
                updateHelmRepo();
            }
            helmCommandExecutor.executeHelmUpgradeCommand(
                    namespaceService.namespace(serviceInfo.getDomain()),
                    serviceInfo.getDescriptiveDeploymentId().getValue(),
                    targetVersion,
                    serviceInfo.getRemoteCluster() != null ? serviceInfo.getRemoteCluster().getPathConfigFile() : null
            );
        } catch (CommandExecutionException cee) {
            throw new KServiceManipulationException(HELM_COMMAND_EXECUTION_FAILED_ERROR_MESSAGE + cee.getMessage());
        }
    }
}
