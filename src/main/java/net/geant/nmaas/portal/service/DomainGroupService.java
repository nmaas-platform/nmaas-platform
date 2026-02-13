package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.domain.DomainGroupView;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DomainGroupService {

    Boolean existDomainGroup(String name, String codeName);

    DomainGroupView createDomainGroup(DomainGroupView domainGroup);

    DomainGroupView addDomainsToGroup(List<Domain> domains, String groupCodeName);

    DomainGroupView deleteDomainFromGroup(Domain domain, Long domainGroupId);

    void deleteDomainGroup(Long domainGroupId);

    DomainGroupView getDomainGroup(Long domainGroupId);

    List<DomainGroupView> getAllDomainGroups();

    Page<DomainGroupView> getPageableAllDomainGroups(Pageable pageable);

    Page<DomainGroupView> getPageableAllDomainGroupsWhereManagerIsMember(Pageable pageable, User manager);

    DomainGroupView updateDomainGroup(Long domainGroupId, DomainGroupView view);

    void deleteAppBaseFromAllAppState(ApplicationBase base);

    void deleteUserFromAllDomainsGroups(User user);
}
