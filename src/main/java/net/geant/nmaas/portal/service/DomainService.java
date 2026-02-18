package net.geant.nmaas.portal.service;

import net.geant.nmaas.api.dto.KeyValueView;
import net.geant.nmaas.api.dto.domains.DomainAnnotationDto;
import net.geant.nmaas.api.dto.domains.DomainBaseDto;
import net.geant.nmaas.api.dto.domains.DomainGroupDto;
import net.geant.nmaas.api.dto.domains.DomainRequest;
import net.geant.nmaas.api.dto.users.UserView;
import net.geant.nmaas.api.dto.users.UserViewMinimal;
import net.geant.nmaas.dcn.deployment.DcnDeploymentType;
import net.geant.nmaas.dcn.deployment.entities.DcnInfo;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.DomainAnnotation;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DomainService {

    Domain createGlobalDomain();

    Optional<Domain> getGlobalDomain();

    List<Domain> getDomains();

    List<Domain> getDomains(String searchValue);

    Page<Domain> getDomains(String searchValue, Pageable pageable);

    boolean existsDomain(String name);

    boolean existsDomainByCodename(String codename);

    boolean existsDomainByExternalServiceDomain(String externalServiceDomain);

    Domain createDomain(DomainRequest request);

    void storeDcnInfo(String domain, DcnDeploymentType dcnDeploymentType);

    void storeDcnInfo(DcnInfo dcnInfo);

    void updateDcnInfo(String domain, DcnDeploymentType dcnDeploymentType);

    Optional<Domain> findDomain(String name);

    Optional<Domain> findDomain(Long id);

    Optional<Domain> findDomainByCodename(String codename);

    void updateDomain(Domain domain);

    Domain changeDcnConfiguredFlag(Long domainId, boolean dcnConfigured);

    void changeDomainState(Long domainId, boolean active);

    boolean removeDomain(Long id);

    List<User> getMembers(Long id);

    void addMemberRole(Long domainId, Long userId, Role role);

    void addGlobalGuestUserRoleIfMissing(Long userId);

    void removeMemberRole(Long domainId, Long userId, Role role);

    void removeMember(Long domainId, Long userId);

    User getMember(Long domainId, Long userId);

    Set<Role> getMemberRoles(Long domainId, Long userId);

    Set<Domain> getUserDomains(Long userId, String searchValue);

    List<UserView> findUsersWithDomainAdminRole(String domain);

    Domain getAppStatesFromGroups(Domain domain);

    boolean softRemoveDomain(Long domainId);

    void removeDomainFromAllGroups(Domain domain);

    void removeAllUsersFromDomain(Domain domain);

    void checkDomainGroupUsers(DomainGroupDto view);

    void updateRolesInDomainGroupByUsers(DomainGroupDto view);

    DomainGroupDto updateMembers(List<UserViewMinimal> newMembers, DomainGroupDto view);

    void addAnnotation(KeyValueView annotation);

    boolean checkIfAnnotationExist(String key);

    void deleteAnnotation(Long id);

    List<DomainAnnotation> getAnnotations();

    void updateAnnotation(Long id, DomainAnnotationDto domainAnnotation);

    void removeAppBaseFromAllDomains(ApplicationBase base);

    List<DomainBaseDto> getDomainsBase(String searchValue);

    Page<DomainBaseDto> getDomainsBase(Pageable pageable, String searchValue);

}