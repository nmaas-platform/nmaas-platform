package net.geant.nmaas.portal.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class DomainRemovalDto {

    private DomainView domainView;
    private boolean hardRemoval;
}
