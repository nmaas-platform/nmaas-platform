package net.geant.nmaas.orchestration;

import java.io.Serializable;

/**
 * Common class for storing various types of identifiers.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class Identifier implements Serializable {

    private String value;

    public Identifier() {}

    public Identifier(String value) {
        this.value = value;
    }

    public static Identifier newInstance(String value) {
        return new Identifier(value);
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

    public String value() {
        return value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
