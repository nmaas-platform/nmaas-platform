package net.geant.nmaas.portal.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AppInstanceBase extends DomainAware {

    private Long id;

    private String applicationName;

    private String applicationVersion;

    private String name;

    private String internalId;

    private UserBase owner;

    private AppInstanceState state;

    private String userFriendlyState;

    private Long createdAt;

    // indicates if automatic upgrades are enabled for this application instance
    private boolean autoUpgradesEnabled = false;
}
