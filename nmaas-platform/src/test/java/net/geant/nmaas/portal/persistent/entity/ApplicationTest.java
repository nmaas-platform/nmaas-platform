package net.geant.nmaas.portal.persistent.entity;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ApplicationTest {

    @Test
    public void shouldBeEqual() {
        assertEquals(new Application(1L, "name1","testversion", "owner"), new Application(1L, "name2","testversion", "owner"));
    }

}
