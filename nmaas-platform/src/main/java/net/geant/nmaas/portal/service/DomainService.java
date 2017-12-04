package net.geant.nmaas.portal.service;

import java.util.List;
import java.util.Set;

import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;

public interface DomainService {
	
	Domain createGlobalDomain();	
	Domain getGlobalDomain();
	
	List<Domain> getDomains();
	
	Domain createDomain(String name);
	Domain findDomain(String name);
	Domain findDomain(Long id);
	
	boolean removeDomain(Long id);
	
	List<User> getMembers(Long id);
	
	void addMemberRole(Long domainId, Long userId, Role role) throws MissingElementException;
	void removeMemberRole(Long domainId, Long userId, Role role) throws MissingElementException;
	void removeMember(Long domainId, Long userId) throws MissingElementException;
	
	User getMember(Long domainId, Long userId) throws MissingElementException;
	Set<Role> getMemberRoles(Long domainId, Long userId) throws MissingElementException;
	
	
}
