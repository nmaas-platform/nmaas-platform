package net.geant.nmaas.portal.persistent.entity;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class AppRateTest {

    @Test
    public void shouldBeEqual() {
        assertEquals(new AppRate.AppRateId(1L, 2L), new AppRate.AppRateId(1L, 2L));
        assertNotEquals(new AppRate.AppRateId(1L, 2L), new AppRate.AppRateId(1L, 3L));
    }

}
