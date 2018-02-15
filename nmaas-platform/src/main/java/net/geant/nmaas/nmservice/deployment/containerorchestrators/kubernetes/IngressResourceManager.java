package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.ingress.IngressResourceManipulationException;
import net.geant.nmaas.orchestration.entities.Identifier;

/**
 * Methods for ingress resource manipulation.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface IngressResourceManager {

    void createOrUpdateIngressResource(Identifier deploymentId, Identifier clientId) throws IngressResourceManipulationException;

    void deleteIngressRule(Identifier deploymentId, Identifier clientId) throws IngressResourceManipulationException;

    void deleteIngressResource(Identifier clientId) throws IngressResourceManipulationException;

}
