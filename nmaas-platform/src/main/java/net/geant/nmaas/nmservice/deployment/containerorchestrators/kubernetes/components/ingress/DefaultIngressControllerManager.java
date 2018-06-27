package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.ingress;

import net.geant.nmaas.externalservices.inventory.kubernetes.KClusterIngressManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.KNamespaceService;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KClusterExtNetworkView;
import net.geant.nmaas.externalservices.inventory.kubernetes.exceptions.ExternalNetworkNotFoundException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.IngressControllerManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmCommandExecutor;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.utils.ssh.CommandExecutionException;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class DefaultIngressControllerManager implements IngressControllerManager {

    private static final String HELM_INSTALL_OPTION_INGRESS_CLASS = "controller.ingressClass";
    private static final String HELM_INSTALL_OPTION_INGRESS_CONTROLLER_EXTERNAL_IPS = "controller.service.externalIPs";
    private static final String NMAAS_INGRESS_CONTROLLER_NAME_PREFIX = "nmaas-icrtl-";
    private static final String NMAAS_INGRESS_CLASS_NAME_PREFIX = "nmaas-iclass-";

    private KClusterIngressManager clusterIngressManager;
    private KNamespaceService namespaceService;
    private HelmCommandExecutor helmCommandExecutor;

    @Autowired
    public DefaultIngressControllerManager(KClusterIngressManager clusterIngressManager, KNamespaceService namespaceService, HelmCommandExecutor helmCommandExecutor) {
        this.clusterIngressManager = clusterIngressManager;
        this.namespaceService = namespaceService;
        this.helmCommandExecutor = helmCommandExecutor;
    }

    @Override
    public void deployIngressControllerIfMissing(String domain) throws IngressControllerManipulationException {
        if(!clusterIngressManager.shouldUseExistingController()) {
            executeDeployIngressControllerIfMissing(domain);
        }
    }

    private void executeDeployIngressControllerIfMissing(String domain) throws IngressControllerManipulationException {
        try {
            String ingressControllerName = ingressControllerName(domain);
            if (checkIfIngressControllerForClientIsMissing(ingressControllerName)) {
                String externalIpAddress = obtainExternalIpAddressForClient(domain);
                installIngressControllerHelmChart(
                        namespaceService.namespace(domain),
                        ingressControllerName,
                        ingressClassName(domain),
                        externalIpAddress);
            }
        } catch (ExternalNetworkNotFoundException ennfe) {
            throw new IngressControllerManipulationException(ennfe.getMessage());
        } catch (CommandExecutionException commandExecutionException) {
            throw new IngressControllerManipulationException("Helm command execution failed -> " + commandExecutionException.getMessage());
        }
    }

    private String obtainExternalIpAddressForClient(String domain) throws ExternalNetworkNotFoundException {
        KClusterExtNetworkView externalNetwork = clusterIngressManager.reserveExternalNetwork(domain);
        return externalNetwork.getExternalIp().getHostAddress();
    }

    private String ingressControllerName(String domain) {
        return NMAAS_INGRESS_CONTROLLER_NAME_PREFIX + domain.toLowerCase();
    }

    private boolean checkIfIngressControllerForClientIsMissing(String ingressControllerName) throws CommandExecutionException {
        List<String> currentReleases = helmCommandExecutor.executeHelmListCommand();
        return !currentReleases.contains(ingressControllerName);
    }

    private String ingressClassName(String domain) {
        return NMAAS_INGRESS_CLASS_NAME_PREFIX + domain;
    }

    private void installIngressControllerHelmChart(String namespace, String releaseName, String ingressClass, String externalIpAddress) throws CommandExecutionException {
        Map<String, String> arguments = new HashMap<>();
        arguments.put(HELM_INSTALL_OPTION_INGRESS_CLASS, ingressClass);
        arguments.put(HELM_INSTALL_OPTION_INGRESS_CONTROLLER_EXTERNAL_IPS, "{" + externalIpAddress + "}");
        helmCommandExecutor.executeHelmInstallCommand(
                namespace,
                releaseName,
                new KubernetesTemplate(
                        clusterIngressManager.getControllerChart(),
                        null,
                        clusterIngressManager.getControllerChartArchive()),
                arguments
        );
    }

    @Override
    public void deleteIngressController(String domain) throws IngressControllerManipulationException {
        if (!clusterIngressManager.shouldUseExistingController()) {
            executeDeleteIngressController(domain);
        }
    }

    private void executeDeleteIngressController(String domain) {
        // TODO add missing functionality
        throw new NotImplementedException();
    }

}
