package net.geant.nmaas.portal.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.kubernetes.remote.api.model.RemoteClusterView;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DomainView extends DomainBase {

    List<ApplicationStatePerDomainView> applicationStatePerDomain;

    DomainTechDetailsView domainTechDetails;

    DomainDcnDetailsView domainDcnDetails;

    List<DomainGroupViewS> groups;

    List<RemoteClusterView> clusters;

}
