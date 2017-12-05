package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class KubernetesTemplateNotFoundException extends Exception {

    public KubernetesTemplateNotFoundException(String message) {
        super(message);
    }

    public KubernetesTemplateNotFoundException(String message, Exception e) {
        super(message, e);
    }
}
