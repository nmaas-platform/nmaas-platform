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

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Identifier that = (Identifier) o;

        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
