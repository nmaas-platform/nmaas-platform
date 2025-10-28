package net.geant.nmaas.portal.persistence.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DomainGroupTest {

    @Test
    void shouldBeEqual() {
        final String commonName = "name";
        final String commonCodeName = "codename";
        assertEquals(new DomainGroup(commonName, commonCodeName), new DomainGroup(commonName, commonCodeName));
    }

}
