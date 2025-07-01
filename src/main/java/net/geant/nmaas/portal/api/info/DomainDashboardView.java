package net.geant.nmaas.portal.api.info;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
public class DomainDashboardView {

    private Map<String, OffsetDateTime> userLogins;
    private Map<String, Integer> applicationDeployed;
    private List<DomainAppInstanceView> applicationUpgradeStatus;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    @ToString
    public static class DomainAppInstanceView {

        private String appName;
        private String instanceName;
        private Long appId;
        private String appVersion;
        private Boolean upgradePossible;
    }
}
