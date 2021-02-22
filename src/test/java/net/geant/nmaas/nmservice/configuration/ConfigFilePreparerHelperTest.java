package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ConfigFilePreparerHelperTest {

    @Test
    public void shouldGenerateNewConfigFileId() {
        assertNotEquals(
                ConfigFilePreparerHelper.generateNewConfigId(Collections.singletonList(new NmServiceConfiguration("old-configuration-id", "", "", ""))),
                "old-configuration-id");
    }

    @Test
    public void shouldGenerateRandomString() {
        assertEquals(32, new ConfigFilePreparerHelper().randomString(32).length());
    }

    @Test
    public void shouldEncodePassword() {
        assertEquals(60, new ConfigFilePreparerHelper().encode("password").length());
    }

}
