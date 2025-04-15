package net.geant.nmaas.portal.api.info;

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
public class DashboardDeploymentsView {

    private String domainName;
    private String user;
    private String applicationName;
    private String applicationVersion;
}
