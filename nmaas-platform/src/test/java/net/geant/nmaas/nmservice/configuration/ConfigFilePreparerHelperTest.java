package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ConfigFilePreparerHelperTest {

    @Test
    public void shouldGenerateNewConfigFileId() {
        assertNotEquals(
                ConfigFilePreparerHelper.generateNewConfigId(Collections.singletonList(new NmServiceConfiguration("old-configuration-id", "", "", ""))),
                "old-configuration-id");
    }

}
