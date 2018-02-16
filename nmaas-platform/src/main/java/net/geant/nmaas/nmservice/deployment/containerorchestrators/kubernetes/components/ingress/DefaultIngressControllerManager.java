package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.ingress;

import net.geant.nmaas.externalservices.inventory.kubernetes.KubernetesClusterManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.ExternalNetworkView;
import net.geant.nmaas.externalservices.inventory.kubernetes.exceptions.ExternalNetworkNotFoundException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.IngressControllerManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster.KNamespaceService;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmCommandExecutor;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.utils.ssh.CommandExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private static final String NMAAS_INGRESS_CONTROLLER_NAME_PREFIX = "nmaas-icrtl-client-";
    private static final String NMAAS_INGRESS_CLASS_NAME_PREFIX = "nmaas-iclass-client-";

    private KubernetesClusterManager clusterManager;
    private KNamespaceService namespaceService;
    private HelmCommandExecutor helmCommandExecutor;

    private String kubernetesIngressControllerChart;

    @Autowired
    public DefaultIngressControllerManager(HelmCommandExecutor helmCommandExecutor, KNamespaceService namespaceService, KubernetesClusterManager clusterManager) {
        this.clusterManager = clusterManager;
        this.namespaceService = namespaceService;
        this.helmCommandExecutor = helmCommandExecutor;
    }

    @Override
    public void deployIngressControllerIfMissing(Identifier clientId) throws IngressControllerManipulationException {
        try {
            String ingressControllerName = ingressControllerName(clientId.value());
            if (checkIfIngressControllerForClientIsMissing(ingressControllerName)) {
                String externalIpAddress = obtainExternalIpAddressForClient(clientId);
                installIngressControllerHelmChart(
                        namespaceService.namespace(clientId),
                        ingressControllerName,
                        ingressClassName(clientId),
                        externalIpAddress);
            }
        } catch (ExternalNetworkNotFoundException ennfe) {
            throw new IngressControllerManipulationException(ennfe.getMessage());
        } catch (CommandExecutionException commandExecutionException) {
            throw new IngressControllerManipulationException("Helm command execution failed -> " + commandExecutionException.getMessage());
        }
    }

    private String obtainExternalIpAddressForClient(Identifier clientId) throws ExternalNetworkNotFoundException {
        ExternalNetworkView externalNetwork = clusterManager.reserveExternalNetwork(clientId);
        return externalNetwork.getExternalIp().getHostAddress();
    }

    private String ingressControllerName(String clientId) {
        return NMAAS_INGRESS_CONTROLLER_NAME_PREFIX + clientId;
    }

    private boolean checkIfIngressControllerForClientIsMissing(String ingressControllerName) throws CommandExecutionException {
        List<String> currentReleases = helmCommandExecutor.executeHelmListCommand();
        return !currentReleases.contains(ingressControllerName);
    }

    private String ingressClassName(Identifier clientId) {
        return NMAAS_INGRESS_CLASS_NAME_PREFIX + clientId;
    }

    private void installIngressControllerHelmChart(String namespace, String releaseName, String ingressClass, String externalIpAddress) throws CommandExecutionException {
        Map<String, String> arguments = new HashMap<>();
        arguments.put(HELM_INSTALL_OPTION_INGRESS_CLASS, ingressClass);
        arguments.put(HELM_INSTALL_OPTION_INGRESS_CONTROLLER_EXTERNAL_IPS, "{" + externalIpAddress + "}");
        helmCommandExecutor.executeHelmInstallCommand(
                namespace,
                releaseName,
                kubernetesIngressControllerChart,
                arguments
        );
    }

    @Override
    public void deleteIngressController(Identifier clientId) throws IngressControllerManipulationException {
        // TODO
    }

    @Value("${kubernetes.ingress.chart}")
    public void setKubernetesIngressControllerChart(String kubernetesIngressControllerChart) {
        this.kubernetesIngressControllerChart = kubernetesIngressControllerChart;
    }

}
