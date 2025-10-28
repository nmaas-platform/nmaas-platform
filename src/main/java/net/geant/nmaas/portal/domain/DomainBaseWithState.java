package net.geant.nmaas.portal.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DomainBaseWithState extends DomainBase {
    List<ApplicationStatePerDomainView> applicationStatePerDomain;
}
