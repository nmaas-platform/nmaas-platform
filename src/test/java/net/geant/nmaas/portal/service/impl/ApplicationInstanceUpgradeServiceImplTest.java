package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.service.ApplicationInstanceUpgradeService;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ApplicationInstanceUpgradeServiceImplTest {

    private final static String CURRENT_CHART_VERSION = "1.2.3";

    ApplicationInstanceUpgradeService appInstanceUpgradeService = new ApplicationInstanceUpgradeServiceImpl();

    @Test
    public void shouldReturnValidNextVersion() {
        Map<String, Long> allVersions = new HashMap<>();
        allVersions.put(CURRENT_CHART_VERSION, 123L);
        allVersions.put("1.2.5", 125L);
        allVersions.put("2.2.4", 224L);
        allVersions.put("1.1.1", 111L);
        allVersions.put("1.3.0", 130L);
        assertEquals(Optional.of(125L), appInstanceUpgradeService.getNextApplicationVersionForUpgrade(CURRENT_CHART_VERSION, allVersions));

        allVersions.put("1.2.4", 124L);
        assertEquals(Optional.of(124L), appInstanceUpgradeService.getNextApplicationVersionForUpgrade(CURRENT_CHART_VERSION, allVersions));

        allVersions.remove("1.2.4");
        allVersions.remove("1.2.5");
        assertEquals(Optional.of(130L), appInstanceUpgradeService.getNextApplicationVersionForUpgrade(CURRENT_CHART_VERSION, allVersions));
    }

    @Test
    public void shouldReturnEmptyNextVersion() {
        Map<String, Long> allVersions = new HashMap<>();
        allVersions.put(CURRENT_CHART_VERSION, 123L);
        allVersions.put("1.2.2", 125L);
        allVersions.put("2.2.4",224L);
        assertFalse(appInstanceUpgradeService.getNextApplicationVersionForUpgrade(CURRENT_CHART_VERSION, allVersions).isPresent());

        allVersions = new HashMap<>();
        assertFalse(appInstanceUpgradeService.getNextApplicationVersionForUpgrade(CURRENT_CHART_VERSION, allVersions).isPresent());
    }

}
