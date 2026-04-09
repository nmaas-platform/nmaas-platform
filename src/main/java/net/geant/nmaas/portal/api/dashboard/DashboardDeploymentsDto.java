package net.geant.nmaas.portal.api.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class DashboardDeploymentsDto {

    private String domainName;
    private String user;
    private String applicationName;
    private String applicationVersion;
    private Long instanceId;
}
