package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.ingress.IngressControllerManipulationException;
import net.geant.nmaas.orchestration.entities.Identifier;

/**
 * Methods for ingress controller manipulation.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface IngressControllerManager {

    void deployIngressControllerIfMissing(Identifier clientId) throws IngressControllerManipulationException;

    void deleteIngressController(Identifier clientId) throws IngressControllerManipulationException;

}
