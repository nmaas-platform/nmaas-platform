package net.geant.nmaas.portal.api.dashboard;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
public class DomainGroupDashboardDto {

    private List<UserLoginsDto> userLogins;
    private List<DomainDto> domains;


    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    @ToString
    public static class DomainDto {

        private String name;
        private List<ApplicationDeployedDto> applicationDeployed;
        private List<DomainAppInstanceDto> applicationUpgradeStatus;

    }
}
