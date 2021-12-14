package net.geant.nmaas.portal.service.impl;

import com.vdurmont.semver4j.Semver;
import net.geant.nmaas.portal.service.ApplicationInstanceUpgradeService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class ApplicationInstanceUpgradeServiceImpl implements ApplicationInstanceUpgradeService {

    /**
     * Calculation logic assumes that Helm chart versions follow the SemVer format guidelines.
     * An application instance with given Helm chart:
     *  - can be only upgraded to a subsequent chart version (not possible to skip versions)
     *  - only minor and patch changes are possible
     */
    @Override
    public Optional<Long> getNextApplicationVersionForUpgrade(String currentHelmChartVersion, Map<String, Long> allVersions) {
        Semver current = new Semver(currentHelmChartVersion);
        Optional<String> next = allVersions.keySet().stream()
                .map(Semver::new)
                .sorted()
                .filter(v -> v.isGreaterThan(current) && !v.diff(current).equals(Semver.VersionDiff.MAJOR))
                .map(Semver::getOriginalValue)
                .findFirst();
        return next.map(allVersions::get);
    }

}
