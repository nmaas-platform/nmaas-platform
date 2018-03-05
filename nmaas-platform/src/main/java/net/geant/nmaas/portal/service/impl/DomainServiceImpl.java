package net.geant.nmaas.portal.service.impl;

import java.util.List;
import java.util.Optional;
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
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;

@Service
public class DomainServiceImpl implements DomainService {

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
		return getGlobalDomain().orElseGet(() -> createDomain(GLOBAL_DOMAIN));
	}
	
	@Override
	public Optional<Domain> getGlobalDomain() {		
		return domainRepo.findByName(GLOBAL_DOMAIN);
	}
	
	
	@Override
	public boolean existsDomain(String name) {
		if(name == null)
			throw new IllegalArgumentException("name is null");
		return domainRepo.existsByName(name);
	}

	@Override
	public Domain createDomain(String name) {		
		return domainRepo.save(new Domain(name));
	}
	
	@Override
	public Optional<Domain> findDomain(String name) {
		return domainRepo.findByName(name);
	}
	
	@Override
	public Optional<Domain> findDomain(Long id) {		
		return Optional.ofNullable(domainRepo.findOne(id));
	}

	
	@Override
	public void updateDomain(Domain domain) {		
		domainRepo.save(domain);
	}

	@Override
	public boolean removeDomain(Long id) {
		return findDomain(id).map(toRemove -> { domainRepo.delete(toRemove); return true;}).orElse(false);		
	}

	@Override
	public List<User> getMembers(Long id) {
		return userRoleRepo.findDomainMembers(id);		
	}

	public void addMemberRole(Long domainId, Long userId, Role role) throws MissingElementException {
		checkParams(domainId, userId);
		checkParams(role);
			
		Domain domain = findDomain(domainId).orElseThrow(() -> new MissingElementException("Domain not found"));
		
		User user = users.findById(userId).orElseThrow(() -> new MissingElementException("User not found"));
		
		if(userRoleRepo.findByDomainAndUserAndRole(domain, user, role) == null)
			userRoleRepo.save(new UserRole(user, domain, role));
	}

	
	
	@Override
	public void removeMemberRole(Long domainId, Long userId, Role role) throws MissingElementException {
		checkParams(domainId, userId);
		checkParams(role);
			
		Domain domain = findDomain(domainId).orElseThrow(() -> new MissingElementException("Domain not found"));
		
		User user = users.findById(userId).orElseThrow(() -> new MissingElementException("User not found"));
		
		userRoleRepo.deleteBy(user, domain, role);
	}

	@Override
	public void removeMember(Long domainId, Long userId) throws MissingElementException {
		checkParams(domainId, userId);
		 
		Domain domain = findDomain(domainId).orElseThrow(() -> new MissingElementException("Domain not found"));
		
		User user = users.findById(userId).orElseThrow(() -> new MissingElementException("User not found"));
		
		userRoleRepo.deleteBy(user, domain);		
	}
	

	@Override
	public Set<Role> getMemberRoles(Long domainId, Long userId) throws MissingElementException {
		checkParams(domainId, userId);
		
		Domain domain = findDomain(domainId).orElseThrow(() -> new MissingElementException("Domain not found"));
		
		User user = users.findById(userId).orElseThrow(() -> new MissingElementException("User not found"));;
		
		return userRoleRepo.findRolesByDomainAndUser(domain, user);
	}

	@Override
	public User getMember(Long domainId, Long userId) throws MissingElementException {
		checkParams(domainId, userId);
		
		Domain domain = findDomain(domainId).orElseThrow(() -> new MissingElementException("Domain not found"));
		
		User user = users.findById(userId).orElseThrow(() -> new MissingElementException("User not found"));;
				
		User userMember = userRoleRepo.findDomainMember(domain, user);
		if(userMember == null)
			throw new ProcessingException("User is not domain member");
		
		return userMember;
	}

	@Override
	public Set<Domain> getUserDomains(Long userId) throws MissingElementException {
		checkParams(userId);

		User user = users.findById(userId).orElseThrow(() -> new MissingElementException("User not found"));
				
		return user.getRoles().stream().map(ur -> ur.getDomain()).collect(Collectors.toSet());
	}

	protected void checkParams(Long userId) {
		if(userId == null)
			throw new IllegalArgumentException("userId is null");
	}
	
	protected void checkParams(Role role) {
		if(role == null)
			throw new IllegalArgumentException("role is null");
	}
	
	protected void checkParams(Long domainId, Long userId) {
		if(domainId == null)
			throw new IllegalArgumentException("domainId is null");
		if(userId == null)
			throw new IllegalArgumentException("userId is null");		
	}
	
}
