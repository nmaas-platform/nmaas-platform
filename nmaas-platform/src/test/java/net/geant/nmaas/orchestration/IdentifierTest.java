package net.geant.nmaas.orchestration;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class IdentifierTest {

    @Test
    public void shouldBeEqual() {
        final String commonValue = "value";
        assertEquals(new Identifier(commonValue), Identifier.newInstance(commonValue));
    }

}
