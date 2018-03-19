package net.geant.nmaas.portal.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;

public interface DomainService {
	
	Domain createGlobalDomain() throws ProcessingException;	
	Optional<Domain> getGlobalDomain();
	
	List<Domain> getDomains();
	Page<Domain> getDomains(Pageable pageable);
	
	boolean existsDomain(String name);
	boolean existsDomainByCodename(String codename);
	
	Domain createDomain(String name, String codename) throws ProcessingException;
	Domain createDomain(String name, String codename, boolean active) throws ProcessingException;
	
	Optional<Domain> findDomain(String name);
	Optional<Domain> findDomain(Long id);
	Optional<Domain> findDomainByCodename(String codename);
	
	void updateDomain(Domain domain) throws ProcessingException;
	boolean removeDomain(Long id);
	
	List<User> getMembers(Long id);
	
	void addMemberRole(Long domainId, Long userId, Role role) throws ObjectNotFoundException;
	void removeMemberRole(Long domainId, Long userId, Role role) throws ObjectNotFoundException;
	void removeMember(Long domainId, Long userId) throws ObjectNotFoundException;
	
	User getMember(Long domainId, Long userId) throws ObjectNotFoundException, ProcessingException;
	Set<Role> getMemberRoles(Long domainId, Long userId) throws ObjectNotFoundException;
	
	Set<Domain> getUserDomains(Long userId) throws ObjectNotFoundException;
}
