package net.geant.nmaas.orchestration;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class AppConfiguration {

    private final Identifier applicationId;

    public AppConfiguration(Identifier applicationId) {
        this.applicationId = applicationId;
    }

    public Identifier getApplicationId() {
        return applicationId;
    }
}
