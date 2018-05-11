package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.ingress;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.IngressResourceManager;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Generates external service URL but does not interact with Kubernetes REST API.
 * It is assumed that ingress resource manipulation is performed by proper preparation of Helm charts.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Profile("kubernetes_api_not_used")
public class DummyIngressResourceManager implements IngressResourceManager {

    // TODO move to cluster object
    private static final String NMAAS_DOMAIN_SUFFIX = ".nmaas.geant.net";

    @Override
    public String createOrUpdateIngressResource(Identifier deploymentId, String domain, String deploymentName) throws IngressResourceManipulationException {
        return externalUrl(deploymentName, domain);
    }

    private String externalUrl(String deploymentName, String domain) {
        return deploymentName + "." + domain.toLowerCase() + NMAAS_DOMAIN_SUFFIX;
    }

    @Override
    public void deleteIngressRule(String serviceExternalUrl, String domain) throws IngressResourceManipulationException {
        // Nothing to do
    }

    @Override
    public void deleteIngressResource(String domain) throws IngressResourceManipulationException {
        // Nothing to do
    }
}
