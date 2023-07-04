package net.geant.nmaas.portal.persistent.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DomainGroupTest {

    @Test
    public void shouldBeEqual() {
        final String commonName = "name";
        final String commonCodeName = "codename";
        assertEquals(new DomainGroup(commonName, commonCodeName), new DomainGroup(commonName, commonCodeName));
    }
}
