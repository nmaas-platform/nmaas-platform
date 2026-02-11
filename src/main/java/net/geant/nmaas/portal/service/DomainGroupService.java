package net.geant.nmaas.portal.service;

import net.geant.nmaas.api.dto.domains.DomainGroupDto;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.User;

import java.util.List;

public interface DomainGroupService {

    Boolean existDomainGroup(String name, String codeName);

    DomainGroupDto createDomainGroup(DomainGroupDto domainGroup);

    DomainGroupDto addDomainsToGroup(List<Domain> domains, String groupCodeName);

    DomainGroupDto deleteDomainFromGroup(Domain domain, Long domainGroupId);

    void deleteDomainGroup(Long domainGroupId);

    DomainGroupDto getDomainGroup(Long domainGroupId);

    List<DomainGroupDto> getAllDomainGroups();

    DomainGroupDto updateDomainGroup(Long domainGroupId, DomainGroupDto view);

    void deleteAppBaseFromAllAppState(ApplicationBase base);

    void deleteUserFromAllDomainsGroups(User user);
}
