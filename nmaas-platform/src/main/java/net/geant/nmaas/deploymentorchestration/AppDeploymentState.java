package net.geant.nmaas.deploymentorchestration;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public enum AppDeploymentState {

    SUBSCRIPTION_VALIDATED,
    DEPLOYMENT_ENVIRONMENT_PREPARED,
    MANAGEMENT_VPN_CONFIGURED,
    APPLICATION_DEPLOYED,
    APPLICATION_DEPLOYMENT_VERIFIED;

}
