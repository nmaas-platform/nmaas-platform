package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.ingress;

import lombok.AllArgsConstructor;
import net.geant.nmaas.externalservices.inventory.kubernetes.KNamespaceService;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.IngressResourceManager;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.stereotype.Component;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * TODO will use an external component (like nmaas-janitor) to create, update and delete ingress resource
 */
@Component
@AllArgsConstructor
public class DefaultIngressResourceManager implements IngressResourceManager {

    private KNamespaceService namespaceService;

    /**
     * Generates URL to be used to access the deployed service from outside of the cluster.
     *
     * @param domain name of the client domain for this deployment
     * @param deploymentName name of the deployment provided by the user
     * @param externalServiceDomain base domain name for external services
     * @param ingressPerDomain indicates which external service domain should be used
     * @return URL under which deployed service is available
     */
    @Override
    public String generateServiceExternalURL(String domain, String deploymentName, String externalServiceDomain, boolean ingressPerDomain) {
        checkArgument(externalServiceDomain != null && !externalServiceDomain.isEmpty(), "External service domain cannot be null or empty");
        return externalUrl(deploymentName.toLowerCase(), domain, externalServiceDomain.toLowerCase(), ingressPerDomain);
    }

    private String externalUrl(String deploymentName, String domain, String externalServiceDomain, boolean ingressPerDomain) {
        if(ingressPerDomain){
            return deploymentName.toLowerCase() + "." + externalServiceDomain.toLowerCase();
        }
        return deploymentName.toLowerCase() + "-" + domain.toLowerCase() + "." + externalServiceDomain.toLowerCase();
    }

    /**
     * Creates new ingress resource if one does not exists or updates the existing one by adding an ingress rule for newly
     * deployed service.
     *
     * @param deploymentId unique identifier of service deployment
     * @param domain name of the client domain for this deployment
     * @param serviceExternalUrl service external URL
     */
    @Override
    @Loggable(LogLevel.INFO)
    public synchronized void createOrUpdateIngressResource(Identifier deploymentId, String domain, String serviceExternalUrl) {
        throw new NotImplementedException();
    }

    /**
     * Removes ingress rule from an existing ingress resource.
     *
     * @param externalServiceUrl external URL assigned for the deployment
     * @param domain name of the domain for this deployment
     */
    @Override
    @Loggable(LogLevel.INFO)
    public synchronized void deleteIngressRule(String externalServiceUrl, String domain) {
        throw new NotImplementedException();
    }

    /**
     * Removes ingress resource for given client.
     *
     * @param domain name of the domain for this deployment
     */
    @Override
    @Loggable(LogLevel.INFO)
    public void deleteIngressResource(String domain) {
        throw new NotImplementedException();
    }

}
