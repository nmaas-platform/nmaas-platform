package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import lombok.AllArgsConstructor;
import net.geant.nmaas.externalservices.kubernetes.KubernetesClusterDeploymentManager;
import net.geant.nmaas.externalservices.kubernetes.KubernetesClusterIngressManager;
import net.geant.nmaas.externalservices.kubernetes.KubernetesClusterNamespaceService;
import net.geant.nmaas.externalservices.kubernetes.model.IngressCertificateConfigOption;
import net.geant.nmaas.externalservices.kubernetes.model.IngressResourceConfigOption;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KServiceLifecycleManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethod;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceStorageVolume;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.exceptions.KServiceManipulationException;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.repositories.DomainTechDetailsRepository;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import net.geant.nmaas.utils.ssh.CommandExecutionException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static net.geant.nmaas.externalservices.kubernetes.model.IngressResourceConfigOption.DEPLOY_FROM_CHART;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesManager.PUBLIC_ACCESS_SELECTOR_ARGUMENT_EXPRESSION_PREFIX;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesManager.RANDOM_ARGUMENT_EXPRESSION_PREFIX;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType.DEFAULT;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType.EXTERNAL;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType.PUBLIC;

@Component
@AllArgsConstructor
@Profile("env_kubernetes")
public class HelmKServiceManager implements KServiceLifecycleManager {

    static final String HELM_INSTALL_OPTION_DEDICATED_WORKERS = "domain";
    static final String HELM_COMMAND_EXECUTION_FAILED_ERROR_MESSAGE = "Helm command execution failed -> ";

    private KubernetesRepositoryManager repositoryManager;
    private KubernetesClusterNamespaceService namespaceService;
    private KubernetesClusterDeploymentManager deploymentManager;
    private KubernetesClusterIngressManager ingressManager;
    private HelmCommandExecutor helmCommandExecutor;
    private DomainTechDetailsRepository domainTechDetailsRepository;

    @Override
    @Loggable(LogLevel.DEBUG)
    public void deployService(Identifier deploymentId) {
        try {
            updateHelmRepo();
            installHelmChart(repositoryManager.loadService(deploymentId));
        } catch (CommandExecutionException cee) {
            throw new KServiceManipulationException(HELM_COMMAND_EXECUTION_FAILED_ERROR_MESSAGE + cee.getMessage());
        }
    }

    private void updateHelmRepo() {
        helmCommandExecutor.executeHelmRepoUpdateCommand();
    }

    private void installHelmChart(KubernetesNmServiceInfo serviceInfo) {
        helmCommandExecutor.executeHelmInstallCommand(
                namespaceService.namespace(serviceInfo.getDomain()),
                serviceInfo.getDescriptiveDeploymentId().getValue(),
                serviceInfo.getKubernetesTemplate(),
                createArgumentsMap(serviceInfo)
        );
    }

    private Map<String, String> createArgumentsMap(KubernetesNmServiceInfo serviceInfo) {
        Map<String, String> arguments = new HashMap<>();
        if (deploymentManager.getForceDedicatedWorkers()) {
            arguments.put(HELM_INSTALL_OPTION_DEDICATED_WORKERS, serviceInfo.getDomain());
        }
        Set<ServiceStorageVolume> serviceStorageVolumes = serviceInfo.getStorageVolumes();
        if (!serviceStorageVolumes.isEmpty()) {
            arguments.putAll(getPersistenceVariables(serviceStorageVolumes, deploymentManager.getStorageClass(serviceInfo.getDomain()), serviceInfo.getDescriptiveDeploymentId().getValue()));
        }
        Set<ServiceAccessMethod> externalAccessMethods = serviceExternalAccessMethods(serviceInfo.getAccessMethods());
        if (!externalAccessMethods.isEmpty()) {
            arguments.putAll(getIngressVariables(ingressManager.getResourceConfigOption(), externalAccessMethods, serviceInfo.getDomain()));
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

    private Map<String, String> getIngressVariables(IngressResourceConfigOption ingressResourceConfigOption, Set<ServiceAccessMethod> externalAccessMethods, String domain){
        return HelmChartVariables.ingressVariablesMap(
                DEPLOY_FROM_CHART.equals(ingressResourceConfigOption),
                externalAccessMethods,
                getIngressClass(domain),
                ingressManager.getPublicIngressClass(),
                ingressManager.getTlsSupported(),
                ingressManager.getIssuerOrWildcardName(),
                ingressManager.getCertificateConfigOption().equals(IngressCertificateConfigOption.USE_LETSENCRYPT)
        );
    }

    private String getIngressClass(String domain){
        if (Boolean.TRUE.equals(ingressManager.getIngressPerDomain())) {
            return domainTechDetailsRepository.findByDomainCodename(domain).orElseThrow(() -> new IllegalArgumentException("DomainTechDetails cannot be found for domain " + domain)).getKubernetesIngressClass();
        }
        return ingressManager.getSupportedIngressClass();
    }

    @Override
    @Loggable(LogLevel.DEBUG)
    public boolean checkServiceDeployed(Identifier deploymentId) {
        try {
            HelmPackageStatus status = helmCommandExecutor.executeHelmStatusCommand(
                    namespaceService.namespace(repositoryManager.loadDomain(deploymentId)),
                    repositoryManager.loadDescriptiveDeploymentId(deploymentId).getValue()
            );
            return status.equals(HelmPackageStatus.DEPLOYED);
        } catch (CommandExecutionException cee) {
            throw new KServiceManipulationException(HELM_COMMAND_EXECUTION_FAILED_ERROR_MESSAGE + cee.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.DEBUG)
    public void deleteServiceIfExists(Identifier deploymentId) {
        String namespace = namespaceService.namespace(repositoryManager.loadDomain(deploymentId));
        Identifier descriptiveDeploymentId = repositoryManager.loadDescriptiveDeploymentId(deploymentId);
        try {
            if(checkIfServiceExists(namespace, descriptiveDeploymentId)){
                helmCommandExecutor.executeHelmDeleteCommand(
                        namespace,
                        descriptiveDeploymentId.getValue()
                );
            }
        } catch (CommandExecutionException cee) {
            throw new KServiceManipulationException(HELM_COMMAND_EXECUTION_FAILED_ERROR_MESSAGE + cee.getMessage());
        }
    }

    private boolean checkIfServiceExists(String namespace, Identifier deploymentId){
        return helmCommandExecutor.executeHelmListCommand(namespace).contains(deploymentId.value());
    }

    @Override
    @Loggable(LogLevel.DEBUG)
    public void upgradeService(Identifier deploymentId, KubernetesTemplate targetVersion) {
        KubernetesNmServiceInfo serviceInfo = repositoryManager.loadService(deploymentId);
        try {
            updateHelmRepo();
            helmCommandExecutor.executeHelmUpgradeCommand(
                    namespaceService.namespace(serviceInfo.getDomain()),
                    serviceInfo.getDescriptiveDeploymentId().getValue(),
                    targetVersion
            );
        } catch (CommandExecutionException cee) {
            throw new KServiceManipulationException(HELM_COMMAND_EXECUTION_FAILED_ERROR_MESSAGE + cee.getMessage());
        }
    }

}
