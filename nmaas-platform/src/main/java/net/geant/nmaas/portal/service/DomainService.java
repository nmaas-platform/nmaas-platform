package net.geant.nmaas.portal.service;

import net.geant.nmaas.dcn.deployment.DcnDeploymentType;
import net.geant.nmaas.portal.api.domain.DomainRequest;
import net.geant.nmaas.portal.api.domain.UserView;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DomainService {
	
	Domain createGlobalDomain();	
	Optional<Domain> getGlobalDomain();
	
	List<Domain> getDomains();
	Page<Domain> getDomains(Pageable pageable);
	
	boolean existsDomain(String name);
	boolean existsDomainByCodename(String codename);
	boolean existsDomainByExternalServiceDomain(String externalServiceDomain);

	Domain createDomain(DomainRequest request);

	void storeDcnInfo(String domain, DcnDeploymentType dcnDeploymentType);
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
	
	Set<Domain> getUserDomains(Long userId);

	List<UserView> findUsersWithDomainAdminRole(String domain);
}