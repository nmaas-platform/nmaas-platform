package net.geant.nmaas.portal.persistent.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationTest {

    @Test
    void shouldBeEqual() {
        assertEquals(new Application(1L, "name1","testversion"), new Application(1L, "name2","testversion"));
    }

}
