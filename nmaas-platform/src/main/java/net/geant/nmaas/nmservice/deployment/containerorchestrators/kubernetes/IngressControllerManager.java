package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.ingress.IngressControllerManipulationException;

/**
 * Methods for ingress controller manipulation.
 */
public interface IngressControllerManager {

    /**
     * If an ingress controller for given domain is missing, creates a new one.
     *
     * @param domain name of the client domain for this deployment
     * @throws IngressControllerManipulationException if any exception is thrown during ingress processing
     */
    void deployIngressControllerIfMissing(String domain) throws IngressControllerManipulationException;

    /**
     * Deletes an ingress controller for given domain.
     *
     * @param domain name of the client domain for this deployment
     * @throws IngressControllerManipulationException if any exception is thrown during ingress processing
     */
    void deleteIngressController(String domain) throws IngressControllerManipulationException;

}
