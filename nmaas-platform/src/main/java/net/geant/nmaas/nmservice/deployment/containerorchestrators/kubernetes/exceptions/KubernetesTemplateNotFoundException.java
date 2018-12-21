package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.exceptions;

public class KubernetesTemplateNotFoundException extends RuntimeException {

    public KubernetesTemplateNotFoundException(String message) {
        super(message);
    }

    public KubernetesTemplateNotFoundException(String message, Exception e) {
        super(message, e);
    }
}
