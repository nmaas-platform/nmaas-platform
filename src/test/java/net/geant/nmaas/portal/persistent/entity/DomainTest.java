package net.geant.nmaas.portal.persistent.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DomainTest {

    @Test
    void shouldBeEqual() {
        final String commonName = "name";
        final String commonCodeName = "codename";
        assertEquals(new Domain(commonName, commonCodeName), new Domain(commonName, commonCodeName));
        assertEquals(new Domain(1L, commonName, commonCodeName), new Domain(2L, commonName, commonCodeName));
    }

}