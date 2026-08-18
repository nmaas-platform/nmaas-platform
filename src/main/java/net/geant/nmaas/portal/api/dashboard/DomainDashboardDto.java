package net.geant.nmaas.portal.api.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
public class DomainDashboardDto {

    private List<UserLoginsDto> userLogins;
    private List<ApplicationDeployedDto> applicationDeployed;
    private List<DomainAppInstanceDto> applicationUpgradeStatus;




}
