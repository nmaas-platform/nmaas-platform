package net.geant.nmaas.deploymentorchestration;

/**
 * Common class for storing various types of identifiers.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class Identifier {

    private final String value;

    public Identifier(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}
