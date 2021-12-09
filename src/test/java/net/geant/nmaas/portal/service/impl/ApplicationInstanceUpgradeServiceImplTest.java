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
        Map<String, String> allVersions = new HashMap<>();
        allVersions.put(CURRENT_CHART_VERSION, "appversion-1.2.3");
        allVersions.put("1.2.5", "appversion-1.2.5");
        allVersions.put("2.2.4", "appversion-2.2.4");
        allVersions.put("1.1.1", "appversion-1.1.1");
        allVersions.put("1.3.0", "appversion-1.3.0");
        assertEquals(Optional.of("appversion-1.2.5"), appInstanceUpgradeService.getNextApplicationVersionForUpgrade(CURRENT_CHART_VERSION, allVersions));

        allVersions.put("1.2.4", "appversion-1.2.4");
        assertEquals(Optional.of("appversion-1.2.4"), appInstanceUpgradeService.getNextApplicationVersionForUpgrade(CURRENT_CHART_VERSION, allVersions));

        allVersions.remove("1.2.4");
        allVersions.remove("1.2.5");
        assertEquals(Optional.of("appversion-1.3.0"), appInstanceUpgradeService.getNextApplicationVersionForUpgrade(CURRENT_CHART_VERSION, allVersions));
    }

    @Test
    public void shouldReturnEmptyNextVersion() {
        Map<String, String> allVersions = new HashMap<>();
        allVersions.put(CURRENT_CHART_VERSION, "appversion-1.2.3");
        allVersions.put("1.2.2", "appversion-1.2.5");
        allVersions.put("2.2.4", "appversion-2.2.4");
        assertFalse(appInstanceUpgradeService.getNextApplicationVersionForUpgrade(CURRENT_CHART_VERSION, allVersions).isPresent());

        allVersions = new HashMap<>();
        assertFalse(appInstanceUpgradeService.getNextApplicationVersionForUpgrade(CURRENT_CHART_VERSION, allVersions).isPresent());
    }

}
