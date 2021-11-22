package net.geant.nmaas.nmservice.deployment.exceptions;

public class CouldNotUpgradeKubernetesServiceException extends RuntimeException {

    public CouldNotUpgradeKubernetesServiceException(String message) {
        super(message);
    }

    public CouldNotUpgradeKubernetesServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
