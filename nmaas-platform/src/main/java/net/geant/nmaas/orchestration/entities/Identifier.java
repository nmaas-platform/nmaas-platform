package net.geant.nmaas.orchestration.entities;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Common class for storing various types of identifiers.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Identifier implements Serializable {

    private String value;

    public static Identifier newInstance(String value) {
        return new Identifier(value);
    }

    public static Identifier newInstance(Long value) {
        return new Identifier(String.valueOf(value));
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

    public Long longValue() {
        return Long.valueOf(value);
    }
}
