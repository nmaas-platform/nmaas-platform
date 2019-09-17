package net.geant.nmaas.portal.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AppInstanceBase extends DomainAware{

    private String applicationName;

    private String name;

    private String internalId;
}
