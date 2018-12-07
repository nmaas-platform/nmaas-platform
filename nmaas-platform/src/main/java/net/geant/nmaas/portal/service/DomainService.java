package net.geant.nmaas.portal.service;

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
	
	Domain createDomain(String name, String codename);
	Domain createDomain(String name, String codename, boolean active);
	Domain createDomain(String name, String codename, boolean active,  boolean dcnConfigured, String kubernetesNamespace, String kubernetesStorageClass, String externalServiceDomain);

	void storeDcnInfo(String domain);

	Optional<Domain> findDomain(String name);
	Optional<Domain> findDomain(Long id);
	Optional<Domain> findDomainByCodename(String codename);
	
	void updateDomain(Domain domain);
	boolean removeDomain(Long id);
	
	List<User> getMembers(Long id);
	
	void addMemberRole(Long domainId, Long userId, Role role);
	void removeMemberRole(Long domainId, Long userId, Role role);
	void removeMember(Long domainId, Long userId);
	
	User getMember(Long domainId, Long userId);
	Set<Role> getMemberRoles(Long domainId, Long userId);
	
	Set<Domain> getUserDomains(Long userId);

	List<User> findUsersWithDomainAdminRole(Long domainId);
}