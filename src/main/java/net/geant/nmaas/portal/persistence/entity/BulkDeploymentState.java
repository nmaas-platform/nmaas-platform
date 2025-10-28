package net.geant.nmaas.portal.persistence.entity;

public enum BulkDeploymentState {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    PARTIALLY_FAILED,

    REMOVED,
    CANCELED,
    PARTIALLY_CANCELED
}
