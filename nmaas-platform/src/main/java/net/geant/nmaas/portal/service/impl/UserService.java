package net.geant.nmaas.portal.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRoleRepository;

@Service
public class UserService implements net.geant.nmaas.portal.service.UserService {

	@Autowired
	net.geant.nmaas.portal.service.DomainService domains;
	
	@Autowired
	UserRepository userRepo;
	
	@Autowired
	UserRoleRepository userRoleRepo;
	
	@Override
	public boolean hasPriviledge(User user, Domain domain, Role role) {
		if(user == null || domain == null || role == null)
			return false;
		
		UserRole userRole = userRoleRepo.findByDomainAndUserAndRole(domain, user, role);		
		
		return (userRole != null);
	}

	@Override
	public List<User> findAll() {		
		return userRepo.findAll();
	}

	@Override
	public Page<User> findAll(Pageable pageable) {		
		return userRepo.findAll(pageable);
	}



	@Override
	public User findByUsername(String username) {
		if(username == null)
			return null;
		return userRepo.findByUsername(username).orElse(null);
	}
	
	@Override
	public User findById(Long id) {
		if(id == null)
			return null;
		return userRepo.findOne(id);
	}

	@Override
	public User register(String username) throws ObjectAlreadyExistsException, MissingElementException {
		return register(username, domains.getGlobalDomain().getName());				
	}

	@Override
	public User register(String username, String domainName) throws ObjectAlreadyExistsException, MissingElementException {
		Domain domain = (domainName != null ? domains.findDomain(domainName) : domains.getGlobalDomain());
		if(domain == null)
			throw new MissingElementException("Domain not found");
		
		User user = userRepo.findByUsername(username).orElse(null);
		if(user != null)
			throw new ObjectAlreadyExistsException("User exists.");

		return userRepo.save(new User(username));				
	}	
	
	@Override
	public void update(User user) throws ProcessingException {
		if(user == null || user.getId() == null)
			throw new ProcessingException("Missing user object");
		
		if(userRepo.findOne(user.getId()) == null)
			throw new ProcessingException("User (id=" + user.getId() + " does not exists.");
		
		userRepo.saveAndFlush(user);
	}

	@Override
	public void delete(User user) throws MissingElementException, ProcessingException {
		if(user == null)
			throw new MissingElementException("user is null");
		if(user.getId() == null)
			throw new ProcessingException("user id is null");
		
		userRepo.delete(user);
	}



	 
	
}
