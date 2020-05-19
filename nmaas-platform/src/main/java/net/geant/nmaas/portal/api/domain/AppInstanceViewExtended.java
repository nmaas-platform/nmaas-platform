package net.geant.nmaas.portal.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AppInstanceViewExtended extends AppInstanceView {

    private DomainBase domain;

    private ApplicationView application;
}
