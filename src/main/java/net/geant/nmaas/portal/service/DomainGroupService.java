package net.geant.nmaas.portal.service;

import net.geant.nmaas.api.dto.domains.DomainGroupDto;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DomainGroupService {

    Boolean existDomainGroup(String name, String codeName);

    DomainGroupDto createDomainGroup(DomainGroupDto domainGroup);

    DomainGroupDto addDomainsToGroup(List<Domain> domains, String groupCodeName);

    DomainGroupDto deleteDomainFromGroup(Domain domain, Long domainGroupId);

    void deleteDomainGroup(Long domainGroupId);

    DomainGroupDto getDomainGroup(Long domainGroupId);

    List<DomainGroupDto> getAllDomainGroups();

    Page<DomainGroupView> getPageableAllDomainGroups(Pageable pageable);

    Page<DomainGroupView> getPageableAllDomainGroupsWhereManagerIsMember(Pageable pageable, User manager);

    DomainGroupDto updateDomainGroup(Long domainGroupId, DomainGroupDto view);

    void deleteAppBaseFromAllAppState(ApplicationBase base);

    void deleteUserFromAllDomainsGroups(User user);
}
