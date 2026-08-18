package net.geant.nmaas.portal.api.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class DomainAppInstanceDto {

    private String appName;
    private String instanceName;
    private Long appId;
    private Long baseAppId;
    private String appVersion;
    private Boolean upgradePossible;
}