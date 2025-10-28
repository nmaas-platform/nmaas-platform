package net.geant.nmaas.portal.persistence.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class AppRateTest {

    @Test
    void shouldBeEqual() {
        assertEquals(new AppRate.AppRateId(1L, 2L), new AppRate.AppRateId(1L, 2L));
        assertNotEquals(new AppRate.AppRateId(1L, 2L), new AppRate.AppRateId(1L, 3L));
    }

}
