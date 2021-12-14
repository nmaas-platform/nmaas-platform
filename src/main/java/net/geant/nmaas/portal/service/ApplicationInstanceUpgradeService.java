package net.geant.nmaas.portal.service;

import java.util.Map;
import java.util.Optional;

public interface ApplicationInstanceUpgradeService {

    /**
     * Determines next version suitable for upgrade from the current one (if such exists)
     *
     * @param currentHelmChartVersion current version of the Helm chart
     * @param allVersions all available Helm chart versions with corresponding app version identifier
     * @return next application version for upgrade
     */
    Optional<Long> getNextApplicationVersionForUpgrade(String currentHelmChartVersion, Map<String, Long> allVersions);

}
