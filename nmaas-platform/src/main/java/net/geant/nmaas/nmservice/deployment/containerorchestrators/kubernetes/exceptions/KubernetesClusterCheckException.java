package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class KubernetesClusterCheckException extends Exception {

    public KubernetesClusterCheckException(String message) {
        super(message);
    }

    public KubernetesClusterCheckException(String message, Exception e) {
        super(message, e);
    }
}
