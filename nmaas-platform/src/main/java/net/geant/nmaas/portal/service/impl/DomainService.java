package net.geant.nmaas.portal.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.ProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.repositories.DomainRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRoleRepository;

@Service
public class DomainService implements net.geant.nmaas.portal.service.DomainService {

	@Value("${domain.global:GLOBAL}")
	String GLOBAL_DOMAIN;
	
	@Autowired
	DomainRepository domainRepo;
	
	@Autowired
	UserService users;
	
	@Autowired
	UserRoleRepository userRoleRepo;
	
	@Override
	public List<Domain> getDomains() {		
		return domainRepo.findAll();
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Domain createGlobalDomain() {
		Domain globalDomain = getGlobalDomain();
		if(globalDomain == null)
			globalDomain = createDomain(GLOBAL_DOMAIN);
		return globalDomain;
	}
	
	@Override
	public Domain getGlobalDomain() {		
		return domainRepo.findByName(GLOBAL_DOMAIN);
	}
	
	@Override
	public Domain createDomain(String name) {		
		return domainRepo.save(new Domain(name));
	}
	
	@Override
	public Domain findDomain(String name) {
		return domainRepo.findByName(name);
	}
	
	@Override
	public Domain findDomain(Long id) {		
		return domainRepo.findOne(id);
	}

	
	@Override
	public void updateDomain(Domain domain) {		
		domainRepo.save(domain);
	}

	@Override
	public boolean removeDomain(Long id) {
		Domain toRemove = findDomain(id);
		if (toRemove != null)
			domainRepo.delete(toRemove);
		else 
			return false;
		
		return true;
	}

	@Override
	public List<User> getMembers(Long id) {
		return userRoleRepo.findDomainMembers(id);		
	}

	public void addMemberRole(Long domainId, Long userId, Role role) throws MissingElementException {
		if(domainId == null)
			throw new IllegalArgumentException("domain is null");
		if(userId == null)
			throw new IllegalArgumentException("user is null");
		if(role == null)
			throw new IllegalArgumentException("role is null");
			
		Domain domain = findDomain(domainId);
		if(domain == null)
			throw new MissingElementException("Domain not found");
		
		User user = users.findById(userId);
		if(user == null)
			throw new MissingElementException("User not found");	
		
		if(userRoleRepo.findByDomainAndUserAndRole(domain, user, role) == null)
			userRoleRepo.save(new UserRole(user, domain, role));
	}

	
	
	@Override
	public void removeMemberRole(Long domainId, Long userId, Role role) throws MissingElementException {
		if(domainId == null)
			throw new IllegalArgumentException("domain is null");
		if(userId == null)
			throw new IllegalArgumentException("user is null");
		if(role == null)
			throw new IllegalArgumentException("role is null");
			
		Domain domain = findDomain(domainId);
		if(domain == null)
			throw new MissingElementException("Domain not found");
		
		User user = users.findById(userId);
		if(user == null)
			throw new MissingElementException("User not found");	
		
		userRoleRepo.deleteBy(user, domain, role);
	}

	@Override
	public void removeMember(Long domainId, Long userId) throws MissingElementException {
		if(domainId == null)
			throw new IllegalArgumentException("domain is null");
		if(userId == null)
			throw new IllegalArgumentException("user is null");
		 
		Domain domain = findDomain(domainId);
		if(domain == null)
			throw new MissingElementException("Domain not found");
		
		User user = users.findById(userId);
		if(user == null)
			throw new MissingElementException("User not found");	
		
		userRoleRepo.deleteBy(user, domain);		
	}
	

	@Override
	public Set<Role> getMemberRoles(Long domainId, Long userId) throws MissingElementException {
		if(domainId == null)
			throw new IllegalArgumentException("domain is null");
		if(userId == null)
			throw new IllegalArgumentException("user is null");

		Domain domain = findDomain(domainId);
		if(domain == null)
			throw new MissingElementException("Domain not found");
		
		User user = users.findById(userId);
		if(user == null)
			throw new MissingElementException("User not found");	
		
		return userRoleRepo.findRolesByDomainAndUser(domain, user);
	}

	@Override
	public User getMember(Long domainId, Long userId) throws MissingElementException {
		if(domainId == null)
			throw new IllegalArgumentException("domain is null");
		if(userId == null)
			throw new IllegalArgumentException("user is null");
		
		Domain domain = findDomain(domainId);
		if(domain == null)
			throw new MissingElementException("Domain not found");
		
		User user = users.findById(userId);
		if(user == null)
			throw new MissingElementException("User not found");	
				
		User userMember = userRoleRepo.findDomainMember(domain, userId);
		if(userMember == null)
			throw new ProcessingException("User is not domain member");
		
		return userMember;
	}

	@Override
	public Set<Domain> getUserDomains(Long userId) throws MissingElementException {
		if(userId == null)
			throw new IllegalArgumentException("user is null");
		User user = users.findById(userId);
		if(user == null)
			throw new MissingElementException("User not found");	
				
		return user.getRoles().stream().map(ur -> ur.getDomain()).collect(Collectors.toSet());
	}

	
	
}
