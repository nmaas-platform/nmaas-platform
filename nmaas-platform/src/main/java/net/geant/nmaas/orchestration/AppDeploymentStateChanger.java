package net.geant.nmaas.orchestration;

/**
 * Declares method to be implemented by every component managing one or more NMaaS application deployment steps.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface AppDeploymentStateChanger {

    /**
     * Method used to add listeners to be notified every time deployment state change takes place.
     *
     * @param stateChangeListener instance of the listener
     */
    void addStateChangeListener(AppDeploymentStateChangeListener stateChangeListener);

}
