package net.geant.nmaas.portal.api.bulk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BulkAppDetails {

    private String userName;

    private String domainCodeName;

    private String appName;

    private String appInstanceName;

    private String appVersion;

    @Builder.Default
    private Map<String, String> parameters = new HashMap<>();

    @Builder.Default
    private Map<String, String> accessMethod = new HashMap<>();
}
