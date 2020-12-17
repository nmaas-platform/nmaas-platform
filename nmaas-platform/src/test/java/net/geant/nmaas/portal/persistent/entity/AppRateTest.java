package net.geant.nmaas.portal.persistent.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class AppRateTest {

    @Test
    public void shouldBeEqual() {
        assertEquals(new AppRate.AppRateId(1L, 2L), new AppRate.AppRateId(1L, 2L));
        assertNotEquals(new AppRate.AppRateId(1L, 2L), new AppRate.AppRateId(1L, 3L));
    }

}
