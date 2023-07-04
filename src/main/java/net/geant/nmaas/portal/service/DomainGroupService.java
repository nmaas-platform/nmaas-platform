package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.domain.DomainGroupView;
import net.geant.nmaas.portal.persistent.entity.Domain;

import java.util.List;

public interface DomainGroupService {

    Boolean existDomainGroup(String name, String codeName);

    DomainGroupView createDomainGroup(DomainGroupView domainGroup);

    DomainGroupView addDomainsToGroup(List<Domain> domains, String groupCodeName);

    DomainGroupView deleteDomainFromGroup(Domain domain, Long domainGroupCodeName);

    void deleteDomainGroup(Long domainGroupId);

    DomainGroupView getDomainGroup(Long domainGroupId);

    List<DomainGroupView> getAllDomainGroups();

    DomainGroupView updateDomainGroup(Long domainGroupId, DomainGroupView view);

}
