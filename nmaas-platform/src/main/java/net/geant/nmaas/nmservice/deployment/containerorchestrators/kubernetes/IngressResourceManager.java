package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.ingress.IngressResourceManipulationException;
import net.geant.nmaas.orchestration.entities.Identifier;

/**
 * Methods for ingress resource manipulation.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface IngressResourceManager {

    /**
     * Generates URL to be used to access the deployed service from outside of the cluster.
     *
     * @param domain name of the client domain for this deployment
     * @param deploymentName name of the deployment provided by the user
     * @param externalServiceDomain base domain name for external services
     * @return URL under which deployed service is available
     */
    String generateServiceExternalURL(String domain, String deploymentName, String externalServiceDomain);

    /**
     * Creates a new ingress resource for given domain or updates an existing one with new entry.
     *
     * @param deploymentId unique identifier of service deployment
     * @param domain name of the client domain for this deployment
     * @param serviceExternalUrl service external URL
     * @throws IngressResourceManipulationException if any exception is thrown during ingress processing
     */
    void createOrUpdateIngressResource(Identifier deploymentId, String domain, String serviceExternalUrl) throws IngressResourceManipulationException;

    /**
     * Deletes a rule from existing ingress resource.
     *
     * @param serviceExternalUrl external URL assigned for the deployment
     * @param domain name of the client domain for this deployment
     * @throws IngressResourceManipulationException if any exception is thrown during ingress processing
     */
    void deleteIngressRule(String serviceExternalUrl, String domain) throws IngressResourceManipulationException;

    /**
     * Deletes the entire ingress resource for given domain.
     *
     * @param domain name of the client domain for this deployment
     * @throws IngressResourceManipulationException if any exception is thrown during ingress processing
     */
    void deleteIngressResource(String domain) throws IngressResourceManipulationException;

}
