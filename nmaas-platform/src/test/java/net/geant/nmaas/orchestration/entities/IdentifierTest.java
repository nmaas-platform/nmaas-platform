package net.geant.nmaas.orchestration.entities;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IdentifierTest {

    @Test
    public void shouldBeEqual() {
        final String commonValue = "value";
        assertEquals(new Identifier(commonValue), Identifier.newInstance(commonValue));
    }

}
