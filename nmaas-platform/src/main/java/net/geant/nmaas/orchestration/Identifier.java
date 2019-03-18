package net.geant.nmaas.orchestration;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Common class for storing various types of identifiers.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Identifier implements Serializable {

    @EqualsAndHashCode.Include
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

    public String value() {
        return value;
    }

    public Long longValue() {
        return Long.valueOf(value);
    }
}
