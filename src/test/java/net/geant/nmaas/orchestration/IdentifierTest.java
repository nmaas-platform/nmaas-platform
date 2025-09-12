package net.geant.nmaas.orchestration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IdentifierTest {

    @Test
    void shouldBeEqual() {
        final String commonValue = "value";
        assertEquals(new Identifier(commonValue), Identifier.newInstance(commonValue));
    }

}
