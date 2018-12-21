package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster;

public class KClusterCheckException extends RuntimeException {

    public KClusterCheckException(String message) {
        super(message);
    }

    public KClusterCheckException(String message, Exception e) {
        super(message, e);
    }
}
